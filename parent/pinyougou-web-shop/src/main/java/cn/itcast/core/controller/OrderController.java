package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.OrderVoo;
import pojogroup.SalesVo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {
    @Reference
    private OrderService orderService;

    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody Order order){
        //商家ID
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setSellerId(name);
        return orderService.search(page,rows,order);
    }

    @RequestMapping("deliverSearch")
    public PageResult deliverSearch(Integer page, Integer rows, @RequestBody Order order){
        //商家ID
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setSellerId(name);
        order.setStatus("3");
        return orderService.search(page,rows,order);
    }


    /**
     * 查询一个实体
     */
    @RequestMapping("findOne")
    public Order findOne(Long id){
        return orderService.findOne(id);

    }



    @RequestMapping("sales")
    public List<OrderVoo> statisticsSales() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String str="2017-08-25 23:49:18";
        Date beginTime = simpleDateFormat.parse(str);
        str = "2017-08-26 11:57:26";
        Date endTime = simpleDateFormat.parse(str);
        return orderService.statisticsSales(beginTime,endTime);

    }


    @RequestMapping("salesLine")
    public List<SalesVo> saleLine(String beginTime,String endTime) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date beginTime1 = simpleDateFormat.parse(beginTime);

        Date endTime1 = simpleDateFormat.parse(endTime);

        return orderService.zhexian(beginTime1,endTime1);
    }



    @RequestMapping("orderDeliver")
    public Result orderDeliver(Long[] ids){
        try {
            orderService.orderDeliver(ids);
            return new Result(true,"已成功发货");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

}
