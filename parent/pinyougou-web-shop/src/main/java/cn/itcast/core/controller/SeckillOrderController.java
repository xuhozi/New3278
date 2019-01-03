package cn.itcast.core.controller;

import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.service.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("seckillOrder")
public class SeckillOrderController {
    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody SeckillOrder seckillOrder){
        return seckillOrderService.search(page,rows,seckillOrder);
    }



}
