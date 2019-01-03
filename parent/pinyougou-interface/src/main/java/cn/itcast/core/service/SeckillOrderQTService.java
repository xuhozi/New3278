package cn.itcast.core.service;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import com.github.pagehelper.PageInfo;
import entity.PageResult;

import java.util.List;

public interface SeckillOrderQTService {
    PageInfo<SeckillOrder> search (Integer page, Integer rows, SeckillOrder seckillOrder);
    List<SeckillOrder> findAll ();
    PageResult findPage (Integer pageNum, Integer pageSize);
}
