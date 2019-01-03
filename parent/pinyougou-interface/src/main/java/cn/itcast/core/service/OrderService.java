package cn.itcast.core.service;

import cn.itcast.core.pojo.order.Order;
import entity.PageResult;
import pojogroup.OrderVoo;
import pojogroup.SalesVo;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public interface OrderService {
    void add (Order order);

    PageResult search (Integer page, Integer rows, Order order);

    int findCount ();

    Order findOne(Long id);

    List<OrderVoo> statisticsSales(Date beginTime, Date endTime);

    List<SalesVo> zhexian(Date beginTime, Date endTime) throws ParseException;

    void orderDeliver(Long[] ids);

    List<Order> selectAll ();
}
