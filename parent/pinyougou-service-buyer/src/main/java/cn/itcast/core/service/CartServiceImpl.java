package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车管理
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ItemDao itemDao;
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }
//将购物车装满
    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Item item=null;
            //商家ID不用了
            //商家名称
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //库存ID-->查询库存
                 item = findItemById(orderItem.getItemId());
                //图片
                orderItem.setPicPath(item.getImage());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()*orderItem.getPrice().doubleValue()));

            }
            //商家名称
            cart.setSellerName(item.getSeller());
        }
        return cartList;
    }
    //合并 新购物车集合到老购物车集合中
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void merge(List<Cart> newcartList, String name) {
        //获取缓存中的购物车结果集
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        //合并新老购物车集合 两大集合大合并
        oldCartList=mergeNewAndOld(newcartList,oldCartList);
        //将老购物车结果集保存到缓存中
        redisTemplate.boundHashOps("CART").put(name,oldCartList);
    }

    @Override
    public List<Cart> findCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }

    //合并新老购物车集合 两大集合大合并
    public List<Cart> mergeNewAndOld(List<Cart> newCartList,List<Cart> oldCartList){
        //判断新车有值
        if (null!=newCartList&&newCartList.size()>0){
            if (null!=oldCartList&&oldCartList.size()>0){
               //新车有值，老车也有值 合并
                for (Cart newCart : newCartList) {

                    //1 添加新购物车
                    //2 判断新购物车的商家是谁，在当前购物车结果集中是否已经存在了
                    int newIndexOf = oldCartList.indexOf(newCart);
                    if (newIndexOf != -1) {
                        //- 存在 从老购物车结果集中找出和哪个新购物车是同一个商家的老购物车
                        Cart oldCart = oldCartList.get(newIndexOf);
                        // 判断新购物车中，新商品 在老购物车中是否存在
                        List<OrderItem> oldCartOrderItemList = oldCart.getOrderItemList();
                        //新购物车中有商品集合
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int indexOf = oldCartOrderItemList.indexOf(newOrderItem);
                            if (indexOf != -1) {//存在
                                //--存在 老商品结果集中哪个商品与新商品一样 数量追加
                                OrderItem oldOrderItem = oldCartOrderItemList.get(indexOf);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            } else {

                                //--不存在 当新商品添加
                                oldCartOrderItemList.add(newOrderItem);
                            }
                        }

                    } else {
                        //-不存在
                        //将新车子放在老车子中
                        oldCartList.add(newCart);
                    }
                }
            }else {
                return newCartList;
            }
        }
        return oldCartList;
    }
}
