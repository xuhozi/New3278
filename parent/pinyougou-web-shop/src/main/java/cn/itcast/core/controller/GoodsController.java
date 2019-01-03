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
        //商家ID
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(name);
        return goodsService.search(page,rows,goods);
    }
    /**
     * 查询一个实体
     */
    @RequestMapping("findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);

    }
    /**
     * 修改
     */
    @RequestMapping("update")
    public Result update(@RequestBody GoodsVo vo){
        try {

            goodsService.update(vo);
            return new Result(true,"成功");
        }catch (Exception e){
            return new Result(false,"失敗");
        }
    }

    @RequestMapping("updateItemStaus")
    public Result update(Long[] ids,String status) {
        try {
//            Long[]ids={149187842867982L};
//            String status ="2";
            goodsService.updateItemStatus(ids,status);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失敗");
        }
    }

}
