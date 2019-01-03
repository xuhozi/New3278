package cn.itcast.core.service;

import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.dao.user.UserDao;
import pojogroup.OrderVo;
import pojogroup.OrderVoo;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderItemQuery;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderItemDao orderItemDao;
    @Override
    public void sendCode(String phone) {
        //1 生成 6位随机数
        String random = RandomStringUtils.randomNumeric(6);
        System.out.println(random);
        //保存验证码到缓存中
        redisTemplate.boundValueOps(phone).set(random);
        //String  o = (String) redisTemplate.boundValueOps(phone).get();
        //System.out.println(o);
        //设置缓存时间
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.DAYS);
        //3 发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phone",phone);
                mapMessage.setString("signName","品优购商城");
                mapMessage.setString("templateCode","SMS_126462276");
                mapMessage.setString("templateParam","{\"number\":\""+random+"\"}");
                return mapMessage;
            }
        });
    }

    @Override
    public void add(User user, String smscode) {
        //从缓存中获取验证码
        String  code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if (null!=code){
            //两次验证码一样不
            if (code.equals(smscode)){
                //两次验证码相同
                user.setCreated(new Date());
                user.setUpdated(new Date());
                userDao.insertSelective(user);
            }else {
                //验证码错误
                throw new RuntimeException("验证码错误");
            }
        }else {
            //验证码失效
            throw new RuntimeException("验证码失效");
        }
    }

    @Override
    public PageResult findPage(Integer page,Integer rows,String userName) {
        List<OrderVo> orderVoList = new ArrayList<>();
        PageHelper.startPage(page,rows);
        OrderQuery orderQuery = new OrderQuery();
        orderQuery.createCriteria().andUserIdEqualTo(userName);
        List<Order> orderList = orderDao.selectByExample(orderQuery);
        for (Order order : orderList) {
            OrderVo vo = new OrderVo();
            vo.setOrder(order);
            OrderItemQuery orderItemQuery = new OrderItemQuery();
            orderItemQuery.createCriteria().andOrderIdEqualTo(order.getOrderId());
            List<OrderItem> orderItemList = orderItemDao.selectByExample(orderItemQuery);
            vo.setOrderItemList(orderItemList);
            orderVoList.add(vo);
        }
        return new PageResult((long) orderList.size(),orderVoList);
    }

    @Override
    public User findOneByUserName(String userName) {
        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(userName);
        return userDao.selectByExample(query).get(0);
    }

    @Override
    public void updateUserDetails(User user) {
        userDao.updateByPrimaryKeySelective(user);
    }

    @Override
    public User showInfo(String userName) {
        UserQuery query = new UserQuery();
        query.createCriteria().andUsernameEqualTo(userName);
        return userDao.selectByExample(query).get(0);
    }
}
