package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.GoodsVo;

/**
 * 商品管理
 */
@RestController
@RequestMapping("goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    /**
     * 添加商品
     * @param vo
     * @return
     */
    @RequestMapping("add")
    public Result add(@RequestBody GoodsVo vo){
        try {
            //当前商家是谁
            String curName = SecurityContextHolder.getContext().getAuthentication().getName();
            vo.getGoods().setSellerId(curName);
            goodsService.add(vo);
            return new Result(true,"成功");
        }catch (Exception e){
           return new Result(false,"失敗");
        }
    }
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){

        return goodsService.search(page,rows,goods);
    }
    /**
     * 审核
     */
    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"审核通过");
        }catch (Exception e){
            return   new Result(false,"审核失败");
        }
    }
    @RequestMapping("delete")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true,"成功");
        }catch (Exception e){
            return   new Result(false,"失败");
        }
    }
}
