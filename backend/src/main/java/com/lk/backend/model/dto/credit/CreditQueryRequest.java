package com.lk.backend.model.dto.credit;

import com.lk.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 积分查询请求体
 * @Author : lk
 * @create 2023/10/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreditQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 创建用户Id
     */
    private Long userId;

    /**
     * 总积分
     */
    private Long creditTotal;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}
