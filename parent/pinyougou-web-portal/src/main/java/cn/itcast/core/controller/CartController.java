package cn.itcast.core.controller;

import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @RequestMapping("addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9003"})//允许跨域访问我
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Cart> cartList = null;
            //Cookie中是否有购物车
            boolean k=false;
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    //获取Cookie中的购物车
                    if ("CART".equals(cookie.getName())) {
                        //获取购物车-->但是Coookie中只存字符串 将字符串转为对象（Json串）
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        k=true;
                        break;
                    }
                }
            }

            //没有 创建购物车对象--cookie的if语句 没走，就是当前没有购物车
            if (null == cartList) {
                cartList = new ArrayList<>();
            }
            //追加当前款  itemId 库存ID, Integer num 数量
            //购物车结果集中放 有条件吗？ 又能要求吗？
            Cart newCart = new Cart();
            //根据库存ID查库存对象
            Item item = cartService.findItemById(itemId);
            //商家ID
            newCart.setSellerId(item.getSellerId());
            //商家名称（不能设置）
            //库存ID
            OrderItem newOrderItem = new OrderItem();
            newOrderItem.setItemId(itemId);
            //数量
            newOrderItem.setNum(num);
            //商品结果集
            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);
            newCart.setOrderItemList(newOrderItemList);
            //1 添加新购物车
            //2 判断新购物车的商家是谁，在当前购物车结果集中是否已经存在了
            int newIndexOf = cartList.indexOf(newCart);
            if (newIndexOf != -1) {
                //- 存在 从老购物车结果集中找出和哪个新购物车是同一个商家的老购物车
                Cart oldCart = cartList.get(newIndexOf);
                // 判断新购物车中，新商品 在老购物车中是否存在
                List<OrderItem> oldCartOrderItemList = oldCart.getOrderItemList();
                int indexOf = oldCartOrderItemList.indexOf(newOrderItem);
                if (indexOf != -1) {//存在
                    //--存在 老商品结果集中哪个商品与新商品一样 数量追加
                    OrderItem oldOrderItem = oldCartOrderItemList.get(indexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                } else {

                    //--不存在 当新商品添加
                    oldCartOrderItemList.add(newOrderItem);
                }
            } else {
                //-不存在
                //将新车子放在老车子中
                cartList.add(newCart);
            }
            //判断是否登录
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //名称是真名 ？ 匿名
            if (!"anonymousUser".equals(name)) {
                //已登录
                //获取Cookie数组
                //获取Cookie中的购物车
                //没有 创建购物车
                //追加 当前款
                //将当前购物车合并到原来的购物车中
                cartService.merge(cartList,name);
                //清空Cookie
                if (k){
                    Cookie cookie=new Cookie("CART",null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            } else {
                //未登录
                //获取Coolie数组

                //将当前购物车保存到Cookie中
                Cookie cookie = new Cookie("CART", JSON.toJSONString(cartList));
                //设置Cookie存活时间
                cookie.setMaxAge(60 * 60 * 24 * 10);
                cookie.setPath("/");
                //回写到浏览器中
                response.addCookie(cookie);

            }
            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            return new Result(false, "加入购物车失败");
        }
    }

    //跳转购物车页面之后，查询购物车结果集
    @RequestMapping("findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) {
        List<Cart> cartList =null;
        Cookie[] cookies = request.getCookies();
        if (null != cookies && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //获取Cookie中的购物车
                if ("CART".equals(cookie.getName())) {
                    //获取购物车-->但是Coookie中只存字符串 将字符串转为对象（Json串）
                    cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                    break;
                }
            }
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //名称是真名 ？ 匿名
        if (!"anonymousUser".equals(name)) {
            //已登录
            //3有 将购物车合并到账户原来购物车 清空Cookie
            if (null!=cartList){
                cartService.merge(cartList,name);
                Cookie cookie=new Cookie("CART",null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            //4将账户中的购物车取出来(从缓存中取出来)
            cartList=cartService.findCartListFromRedis(name);

        }

        //5 有 将购物车结果集装满
        if (null!=cartList){
            cartList=cartService.findCartList(cartList);
        }
        //6 回显
        return cartList;
    }
}
