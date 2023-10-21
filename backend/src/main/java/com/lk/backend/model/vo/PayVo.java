package com.lk.backend.model.vo;

import lombok.Data;

/**
 * 支付返回类
 * @Author : lk
 * @create 2023/10/24
 */
@Data
public class PayVo {
    // 商户订单号 必填
    private String alipayTradeNo;
    // 订单名称 必填
    private String subject;
    // 付款金额 必填
    private String total_amount;
    // 商品描述 可空
    private String body;
}
