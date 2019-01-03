package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import pojogroup.OrderVoo;
import pojogroup.SalesVo;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单管理
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private PayLogDao payLogDao;

    @Autowired
    private GoodsDao goodsDao;
    //保存订单主表(购物车)  订单详情表

    @Override
    public void add (Order order) {
        //支付总金额
        double tp = 0;
        //订单ID集合
        List <String> ids = new ArrayList <> ();
        List <Cart> cartList = (List <Cart>) redisTemplate.boundHashOps ("CART").get (order.getUserId ());
        for (Cart cart : cartList) {
            //订单ID 分布式ID生成器
            long id = idWorker.nextId ();
            ids.add (String.valueOf (id));
            order.setOrderId (id);
            //实付金额
            double totalPrice = 0;
            //状态
            order.setStatus ("1");
            //创建时间
            order.setCreateTime (new Date ());
            //更新时间
            order.setUpdateTime (new Date ());
            //来源
            order.setSourceType ("2");
            //商家ID
            order.setSellerId (cart.getSellerId ());

            //保存订单详情
            List <OrderItem> orderItemList = cart.getOrderItemList ();
            for (OrderItem orderItem : orderItemList) {
                //库存ID 数量
                Item item = itemDao.selectByPrimaryKey (orderItem.getItemId ());
                //ID
                long orderItemId = idWorker.nextId ();
                orderItem.setId (orderItemId);
                //商品ID
                orderItem.setGoodsId (item.getGoodsId ());
                //订单ID
                orderItem.setOrderId (id);
                //标题
                orderItem.setTitle (item.getTitle ());
                //图片
                orderItem.setPicPath (item.getImage ());
                //商家ID
                orderItem.setSellerId (item.getSellerId ());
                //单价
                orderItem.setPrice (item.getPrice ());
                //小计
                orderItem.setTotalFee (new BigDecimal (item.getPrice ().doubleValue () * orderItem.getNum ()));
                totalPrice += orderItem.getTotalFee ().doubleValue ();
                //保存订单详情
                orderItemDao.insertSelective (orderItem);
            }

            //实付金额
            order.setPayment (new BigDecimal (totalPrice));
            tp += order.getPayment ().doubleValue ();
            //保存订单
            orderDao.insertSelective (order);
        }
        //删除
        redisTemplate.boundHashOps ("CART").delete (order.getUserId ());
        //两张订单表和成一张支付日志表
        PayLog payLog = new PayLog ();
        //ID
        long outTradeNo = idWorker.nextId ();
        payLog.setOutTradeNo (String.valueOf (outTradeNo));
        //创建时间
        payLog.setCreateTime (new Date ());
        //用户ID
        payLog.setUserId (order.getUserId ());
        //支付状态
        payLog.setTradeState ("0");
        //总金额
        payLog.setTotalFee ((long) tp * 100);
        //订单集合
        payLog.setOrderList (ids.toString ().replace ("[", "").replace ("]", ""));
        //在线
        payLog.setPayType ("1");
        payLogDao.insertSelective (payLog);
        //放入缓存一份
        redisTemplate.boundHashOps ("payLog").put (order.getUserId (), payLog);
    }

    //查询订单
    @Override
    public PageResult search (Integer page, Integer rows, Order order) {
        PageHelper.startPage (page, rows);
        OrderQuery orderQuery = new OrderQuery ();
        if (null != order.getUserId ()) {
            OrderQuery.Criteria criteria = orderQuery.createCriteria ().andSellerIdEqualTo (order.getUserId ());
            Page <Order> o = (Page <Order>) orderDao.selectByExample (orderQuery);
            return new PageResult (o.getTotal (), o.getResult ());
        }
        if (null != order.getSellerId ()) {
            OrderQuery.Criteria criteria = orderQuery.createCriteria ().andSellerIdEqualTo (order.getSellerId ());
            Page <Order> o = (Page <Order>) orderDao.selectByExample (orderQuery);
            return new PageResult (o.getTotal (), o.getResult ());
        }
        Page <Order> o = (Page <Order>) orderDao.selectByExample (null);
        return new PageResult (o.getTotal (), o.getResult ());
    }

    @Override
    public Order findOne (Long id) {

        return orderDao.selectByPrimaryKey (id);
    }

    //查询所有订单数
    @Override
    public int findCount () {
        return orderDao.countByExample (null);
    }

    @Override
    public List <OrderVoo> statisticsSales (Date beginTime, Date endTime) {
        OrderQuery orderQuery = new OrderQuery ();
        OrderQuery.Criteria criteria = orderQuery.createCriteria ().andCreateTimeBetween (beginTime, endTime);
        List <Order> orders = orderDao.selectByExample (orderQuery);
        OrderItemQuery orderItemQuery = new OrderItemQuery ();

        List <OrderVoo> list = new ArrayList <> ();

        if (orders.size () > 0 && null != orders) {
            for (Order order : orders) {
                OrderVoo orderVoo = new OrderVoo ();
                orderVoo.setCreateTime (order.getCreateTime ());
                OrderItemQuery.Criteria criteria1 = orderItemQuery.createCriteria ().andOrderIdEqualTo (order.getOrderId ());
                List <OrderItem> orderItems = orderItemDao.selectByExample (orderItemQuery);
                for (OrderItem orderItem : orderItems) {
                    String goodsName = goodsDao.selectByPrimaryKey (orderItem.getGoodsId ()).getGoodsName ();
                    BigDecimal totalFee = orderItem.getTotalFee ();
                    orderVoo.setGoods (goodsName);
                    orderVoo.setSales (totalFee.intValue ());
                    if (list.indexOf (orderVoo) != -1) {
                        OrderVoo orderVoo1 = list.get (list.indexOf (orderVoo));
                        orderVoo1.setSales (orderVoo.getSales () + orderVoo.getSales ());
                    } else {
                        list.add (orderVoo);
                    }
                }
            }

        }
        return list;
    }

    @Override
    public List <SalesVo> zhexian (Date beginTime, Date endTime) throws ParseException {
        //每天销售额
        OrderQuery orderQuery = new OrderQuery ();
        List <SalesVo> list = new ArrayList <> ();
        SalesVo salesVo = new SalesVo ();
        OrderQuery.Criteria criteria = orderQuery.createCriteria ().andCreateTimeBetween (beginTime, endTime);
        List <Order> orders = orderDao.selectByExample (orderQuery);
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
        for (Order order : orders) {
            String s = sdf.format (order.getCreateTime ());
            Date day = sdf.parse (s);
            salesVo.setDay (day);
            salesVo.setPay (order.getPayment ().intValue ());
            if (list.size () > 0) {
                int indexOf = list.indexOf (salesVo);
                if (indexOf != -1) {
                    SalesVo s1 = new SalesVo ();
                    s1.setDay (salesVo.getDay ());
                    s1.setPay (salesVo.getPay ());

                    SalesVo s3 = list.get (indexOf);

                    s3.setPay (s3.getPay () + s1.getPay ());

                } else {
                    SalesVo s2 = new SalesVo ();
                    s2.setDay (salesVo.getDay ());
                    s2.setPay (salesVo.getPay ());
                    list.add (s2);
                }
            } else {
                SalesVo s4 = new SalesVo ();
                s4.setDay (salesVo.getDay ());
                s4.setPay (salesVo.getPay ());
                list.add (s4);
            }

        }

        return list;
    }

    @Override
    public void orderDeliver (Long[] ids) {
        for (Long id : ids) {
            Order order = new Order ();
            order.setStatus ("4");
            order.setOrderId (id);
            order.setShippingCode ("" + idWorker.nextId ());
            orderDao.updateByPrimaryKeySelective (order);
        }
    }
}
