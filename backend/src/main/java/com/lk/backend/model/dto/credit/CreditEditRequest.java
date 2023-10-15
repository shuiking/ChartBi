package com.lk.backend.model.dto.credit;

import lombok.Data;

import java.io.Serializable;

/**
 * 积分编辑请求
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class CreditEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 总积分
     */
    private Long creditTotal;

    private static final long serialVersionUID = 1L;
}
