package cn.itcast.core.service;

import cn.itcast.common.utils.HttpClient;
import cn.itcast.core.pojo.log.PayLog;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付管理
 */
@Service
public class PayServiceImpl implements PayService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    //连接微信服务器端 发出请求 响应 二维码的地址
    @Override
    public Map<String, String> createNative(String name) {
       //支付订单号
        //总金额
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(name);
        //1.创建参数
        Map<String,String> param=new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", payLog.getOutTradeNo());//商户订单号
      //  param.put("total_fee",String.valueOf(payLog.getTotalFee()));//总金额（分）
        param.put("total_fee","1");//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://www.itcast.cn");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);
           // Map<String, String> map=new HashMap<>();
          // map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", String.valueOf(payLog.getTotalFee()));//总金额
            map.put("out_trade_no",payLog.getOutTradeNo());//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        Map<String,String> param=new HashMap<>();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client=new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

}
