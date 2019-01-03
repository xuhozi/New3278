package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Reference
    private OrderService orderService;
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody Order order){
        return orderService.search (page,rows,order);
    }

    @RequestMapping("findCount")
    public int findCount(){
       return orderService.findCount();
    }


}
