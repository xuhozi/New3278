package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("add")
    public Result add(@RequestBody Seller seller){
      try {
          sellerService.add(seller);
          return  new Result(true,"成功");
      }catch (Exception e){
          return  new Result(false,"失败");
      }
    }
    /**
     * 根据条件分页查询
     */
    @RequestMapping("search")
    public PageResult search(Integer page,Integer rows,Seller seller){
        return sellerService.search(page,rows,seller);
    }
}
