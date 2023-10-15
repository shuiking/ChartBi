package com.lk.backend.model.dto.order;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单更新请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class OrderUpdateRequest implements Serializable {

    /**
     * 订单id
     */
    private Long id;

    /**
     * 支付宝交易凭证id
     */
    private String alipayTradeNo;

    /**
     * unpaid,paying,succeed,failed
     */
    private String tradeStatus;

    /**
     * 支付宝买家id
     */
    private String buyerId;


    private static final long serialVersionUID = 1L;
}
