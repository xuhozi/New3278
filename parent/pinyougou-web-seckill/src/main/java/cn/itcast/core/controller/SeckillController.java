package cn.itcast.core.controller;

import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.service.SeckillGoodsService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 秒杀商品信息  LH
 */
@RestController
@RequestMapping("seckillGoods")
public class SeckillController {

    @Reference
    private SeckillGoodsService seckillGoodsService;


    @RequestMapping("findList")
    public List<SeckillGoods> findList(){
        return seckillGoodsService.findList();
    }

    @RequestMapping("findOneFromRedis")
    public SeckillGoods findOneFromRedis(Long id){
        return seckillGoodsService.findOneFromRedis(id);
    }
}