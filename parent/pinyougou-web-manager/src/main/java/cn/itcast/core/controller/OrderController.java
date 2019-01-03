package cn.itcast.core.controller;

import cn.itcast.common.utils.ExportExcel;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @RequestMapping("importData")
    public Result importData(){
        try {
            String sheetName="订单表";
            String titleName="Orders";
            String[] headers = { "order_id", "payment", "payment_type","post_fee","status","create_time", "update_time","payment_time"
                    ,"consign_time","end_time","close_time","shopping_name","shopping_code","user_id","buyer_message","buyer_nick","buyer_rate"
                    ,"receiver_area_name","receiver_mobile","recevier_zip_code","receiver"
                    ,"expire","invoice_type","source_type","seller_id","time"};
            List<Order> goodsList = orderService.selectAll();

            String resultUrl="D:\\orders.xls"; String pattern="yyyy-MM-dd";
            ExportExcel.exportExcel(sheetName, titleName, headers,goodsList, resultUrl, pattern);
            return new Result(true,"导出成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"导出失败");
        }
    }



}
