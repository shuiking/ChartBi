package com.lk.backend.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lk.backend.model.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 * @Author : lk
 * @create 2023/10/24
 */
@Data
@ConfigurationProperties(prefix = "alipay")
@Component
public class AlipayTemplate {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    // 服务器[异步通知]页面路径,支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://psgp9tec.shenzhuo.vip:33664/userApi/alipay/notify";
    //同步通知，支付成功
    private String return_url = "http://localhost:8000/user/edit";
    // 签名方式
    private String sign_type = "RSA2";
    // 字符编码格式
    private String charset = "utf-8";
    // 支付宝网关
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {
        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl, appId, appPrivateKey, "json", charset, alipayPublicKey, sign_type);
        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);
        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getAlipayTradeNo();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();
        //<editor-fold defaultstate="collapsed" desc="delombok">
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\",\"total_amount\":\"" + total_amount + "\",\"subject\":\"" + subject + "\",\"body\":\"" + body + "\",\"timeout_express\":\"1m\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        return alipayClient.pageExecute(alipayRequest).getBody();
    }
}
