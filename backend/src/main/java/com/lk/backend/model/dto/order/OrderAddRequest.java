package com.lk.backend.model.dto.order;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单创建请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class OrderAddRequest implements Serializable {

    /**
     * 交易名称
     */
    private String subject;

    /**
     * 交易金额
     */
    private Double totalAmount;

    private static final long serialVersionUID = 1L;
}
