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
 * 关于秒杀商品的支付   LH
 */
@Service
public class PayServiceImpl implements PayService {


    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.构建请求参数
        Map paramMap=new HashMap();
        paramMap.put("appid", appid);
        paramMap.put("mch_id", partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body", "品优购");
        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("total_fee", total_fee);
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "https://www.baidu.com");
        paramMap.put("trade_type", "NATIVE");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //2.发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String resultXml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            Map map=new HashMap();
            map.put("code_url",resultMap.get("code_url"));
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }


    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1.构建请求参数
        Map paramMap=new HashMap();
        paramMap.put("appid", appid);
        paramMap.put("mch_id", partner);
        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //2.发送请求
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3.获取结果
            String resultXml = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(resultXml);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Map queryPayStatusWhile(String out_trade_no) {
        Map map=null;
        int x=0;
        while (true){
            x++;
            if (x>=10){
                break;
            }
            map = queryPayStatus(out_trade_no);

            if (map==null){
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                break;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    @Override
    public Map<String, String> createNative(String name) {
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(name);
        Map<String, String> param = new HashMap<>();
        param.put("appid", appid);
        param.put("mch_id", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body", "品优购");
        param.put("out_trade_no", payLog.getOutTradeNo());
        param.put("total_fee", "1");
        param.put("spbill_create_ip", "127.0.0.1");
        param.put("notify_url", "www.itcast.cn");
        param.put("trade_type", "NATIVE");

        try {
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xml);
            httpClient.post();
            String result = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            map.put("out_trade_no",payLog.getOutTradeNo());
            map.put("total_fee",String.valueOf(payLog.getTotalFee()));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        Map param = new HashMap();
        param.put("appid", appid);//公众账号 ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param,partnerkey);
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String,String>map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

