package cn.itcast.core.service;

import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import entity.PageResult;

import java.util.List;

public interface SeckillGoodsService {
    PageResult search (Integer page, Integer rows, SeckillGoods seckillGoods);

    void updateStatus (Long[] ids, String status);

    List <SeckillGoods> findList ();

    SeckillGoods findOneFromRedis (Long id);

}
