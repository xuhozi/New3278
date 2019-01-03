package cn.itcast.core.service;

import java.util.Map;

public interface PayService {
    Map<String,String> createNative(String name);

    Map createNative(String out_trade_no,String total_fee);
    Map queryPayStatus(String out_trade_no);

    //  实时查询商品支付状态   LH
    Map queryPayStatusWhile(String out_trade_no);


    //关闭支付订单
    Map closePay(String out_trade_no);
}
