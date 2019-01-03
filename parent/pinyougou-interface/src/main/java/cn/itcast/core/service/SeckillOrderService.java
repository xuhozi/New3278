package cn.itcast.core.service;

import cn.itcast.core.pojo.seckill.SeckillOrder;
import entity.PageResult;

public interface SeckillOrderService {
    PageResult search (Integer page, Integer rows, SeckillOrder seckillOrder);

    void submitOrder (Long seckillId, String userId);

    SeckillOrder searchOrderFromRedis (String userId);

    void saveOrderFromRedis (String userId, Long orderId, String transactionId);

    void deleteOrderFromRedis (String userId, Long orderId);
}
