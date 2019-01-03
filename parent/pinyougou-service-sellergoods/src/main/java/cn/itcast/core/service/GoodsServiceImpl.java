package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.GoodsVo;

import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("Duplicates")
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;

    @Override
    public void add (GoodsVo vo) {
        //商品表
        //设置商品 状态为0
        vo.getGoods ().setAuditStatus ("0");
        goodsDao.insertSelective (vo.getGoods ());
        //商品詳情表
        //将上面商品表的id塞给商品详情表的id
        vo.getGoodsDesc ().setGoodsId (vo.getGoods ().getId ());
        goodsDescDao.insertSelective (vo.getGoodsDesc ());
        //庫存表
        //判断是否启用规格
        if ("1".equals (vo.getGoods ().getIsEnableSpec ())) {
            //启用
            //库存表 多个
            List <Item> itemList = vo.getItemList ();
            for (Item item : itemList) {
                //标题 == 商品名称 + " " + 规格1 + " " 规格2
                String title = vo.getGoods ().getGoodsName ();
                //{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec ();
                Map <String, String> specMap = JSON.parseObject (spec, Map.class);
                Set <Map.Entry <String, String>> entries = specMap.entrySet ();
                for (Map.Entry <String, String> entry : entries) {
                    title += " " + entry.getValue ();
                }
                item.setTitle (title);
                //商品的第一张图片
                //[{"color":"黄色","url":"http://192.168.200.128/group1/M00/00/01/wKjIgFq7QDWACj-5AAHTwjU3Qf4939.png"},{"url":"http://192.168.200.128/group1/M00/00/01/wKjIgFq7QESAdfWBAABJC2MBOwc105.jpg"}]
                String itemImages = vo.getGoodsDesc ().getItemImages ();
                List <Map> images = JSON.parseArray (itemImages, Map.class);
                if (null != images && images.size () > 0) {
                    item.setImage ((String) images.get (0).get ("url"));
                }
                //商品分类 3级ID
                item.setCategoryid (vo.getGoods ().getCategory3Id ());
                //商品分类 3级名称
                ItemCat itemCat = itemCatDao.selectByPrimaryKey (vo.getGoods ().getCategory3Id ());
                item.setCategory (itemCat.getName ());
                //添加时间
                item.setCreateTime (new Date ());
                //修改时间
                item.setUpdateTime (new Date ());
                //商品表的ID  本表的外键
                item.setGoodsId (vo.getGoods ().getId ());
                //商家ID
                item.setSellerId (vo.getGoods ().getSellerId ());
                //商家的公司名称
                Seller seller = sellerDao.selectByPrimaryKey (vo.getGoods ().getSellerId ());
                item.setSeller (seller.getName ());
                //品牌名称
                Brand brand = brandDao.selectByPrimaryKey (vo.getGoods ().getBrandId ());
                item.setBrand (brand.getName ());
                //保存
                itemDao.insertSelective (item);
            }

        } else {
            //不启用  默认值
        }

    }

    @Override
    public PageResult search (Integer page, Integer rows, Goods goods) {
        PageHelper.startPage (page, rows);
        PageHelper.orderBy ("id desc");
        GoodsQuery goodsQuery = new GoodsQuery ();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria ();
        if (null != goods.getAuditStatus () && !"".equals (goods.getAuditStatus ())) {
            criteria.andAuditStatusEqualTo (goods.getAuditStatus ());
        }
        if (null != goods.getGoodsName () && !"".equals (goods.getGoodsName ().trim ())) {
            criteria.andGoodsNameLike ("%" + goods.getGoodsName ().trim () + "%");
        }
        //商家的ID不为null,商家查询，否则运营商查询
        if (null != goods.getSellerId ()) {
            //只查询当前登陆人的所有商品
            criteria.andSellerIdEqualTo (goods.getSellerId ());
        }
        //只查询不删除的
        criteria.andIsDeleteIsNull ();
        Page <Goods> p = (Page <Goods>) goodsDao.selectByExample (goodsQuery);
        return new PageResult (p.getTotal (), p.getResult ());
    }

    //删除
    public void delete (Long[] ids) {
        Goods goods = new Goods ();
        goods.setIsDelete ("1");
        for (Long id : ids) {
            //更新MySql商品表已删除字段 为1
            goods.setId (id);
            goodsDao.updateByPrimaryKeySelective (goods);
            //发消息
            jmsTemplate.send (queueSolrDeleteDestination, new MessageCreator () {
                @Override
                public Message createMessage (Session session) throws JMSException {
                    return session.createTextMessage (String.valueOf (id));
                }
            });

            //删除索引库中索引
        }
    }

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;

    @Override
    public void updateStatus (Long[] ids, String status) {
        Goods goods = new Goods ();
        goods.setAuditStatus (status);
        //商品表ID
        for (Long id : ids) {
            goods.setId (id);
            //1:商品状态更改
            goodsDao.updateByPrimaryKeySelective (goods);
            //判断一定是通过
            if ("1".equals (status)) {
                //发消息
                jmsTemplate.send (topicPageAndSolrDestination, new MessageCreator () {
                    @Override
                    public Message createMessage (Session session) throws JMSException {
                        return session.createTextMessage (String.valueOf (id));
                    }
                });
            }

        }
    }

    @Override
    public GoodsVo findOne (Long id) {
        GoodsVo vo = new GoodsVo ();
        //商品表
        vo.setGoods (goodsDao.selectByPrimaryKey (id));
        //商品详情表
        vo.setGoodsDesc (goodsDescDao.selectByPrimaryKey (id));
        //库存表
        ItemQuery itemQuery = new ItemQuery ();
        itemQuery.createCriteria ().andGoodsIdEqualTo (id);
        List <Item> items = itemDao.selectByExample (itemQuery);
        vo.setItemList (items);
        return vo;
    }

    @Override
    public void update (GoodsVo vo) {
        //商品表
        goodsDao.updateByPrimaryKeySelective (vo.getGoods ());
        //商品详情表
        goodsDescDao.updateByPrimaryKeySelective (vo.getGoodsDesc ());
        //库存表
        //先删除--条件根据id删除
        ItemQuery itemQuery = new ItemQuery ();
        itemQuery.createCriteria ().andGoodsIdEqualTo (vo.getGoods ().getId ());
        itemDao.deleteByExample (itemQuery);

        //在添加
        //判断是否启用规格
        if ("1".equals (vo.getGoods ().getIsEnableSpec ())) {
            //启用
            //库存表 多个
            List <Item> itemList = vo.getItemList ();
            for (Item item : itemList) {
                //标题 == 商品名称 + " " + 规格1 + " " 规格2
                String title = vo.getGoods ().getGoodsName ();
                //{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec ();
                Map <String, String> specMap = JSON.parseObject (spec, Map.class);
                Set <Map.Entry <String, String>> entries = specMap.entrySet ();
                for (Map.Entry <String, String> entry : entries) {
                    title += " " + entry.getValue ();
                }
                item.setTitle (title);
                //商品的第一张图片
                //[{"color":"黄色","url":"http://192.168.200.128/group1/M00/00/01/wKjIgFq7QDWACj-5AAHTwjU3Qf4939.png"},{"url":"http://192.168.200.128/group1/M00/00/01/wKjIgFq7QESAdfWBAABJC2MBOwc105.jpg"}]
                String itemImages = vo.getGoodsDesc ().getItemImages ();
                List <Map> images = JSON.parseArray (itemImages, Map.class);
                if (null != images && images.size () > 0) {
                    item.setImage ((String) images.get (0).get ("url"));
                }
                //商品分类 3级ID
                item.setCategoryid (vo.getGoods ().getCategory3Id ());
                //商品分类 3级名称
                ItemCat itemCat = itemCatDao.selectByPrimaryKey (vo.getGoods ().getCategory3Id ());
                item.setCategory (itemCat.getName ());
                //添加时间
                item.setCreateTime (new Date ());
                //修改时间
                item.setUpdateTime (new Date ());
                //商品表的ID  本表的外键
                item.setGoodsId (vo.getGoods ().getId ());
                //商家ID
                item.setSellerId (vo.getGoods ().getSellerId ());
                //商家的公司名称
                Seller seller = sellerDao.selectByPrimaryKey (vo.getGoods ().getSellerId ());
                item.setSeller (seller.getName ());
                //品牌名称
                Brand brand = brandDao.selectByPrimaryKey (vo.getGoods ().getBrandId ());
                item.setBrand (brand.getName ());
                //保存
                itemDao.insertSelective (item);
            }

        } else {
            //不启用  默认值
        }
    }

    @Override
    public void updateItemStatus (Long[] ids, String status) {
        System.out.println (status);
        Item item = new Item ();
        item.setStatus (status);
        for (Long id : ids) {
            itemDao.updateByPrimaryKeySelective (item);
            //发消息
            jmsTemplate.send (topicPageAndSolrDestination, new MessageCreator () {
                @Override
                public Message createMessage (Session session) throws JMSException {
                    MapMessage mapMessage = session.createMapMessage ();
                    mapMessage.setString ("id", String.valueOf (id));
                    mapMessage.setString ("status", status);
                    return mapMessage;
                }
            });
        }

    }
}
