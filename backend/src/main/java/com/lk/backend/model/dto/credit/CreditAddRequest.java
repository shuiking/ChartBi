package com.lk.backend.model.dto.credit;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class CreditAddRequest implements Serializable {
    /**
     * 创建用户Id
     */
    private Long userId;

    /**
     * 总积分
     */
    private Long creditTotal;



    private static final long serialVersionUID = 1L;
}
