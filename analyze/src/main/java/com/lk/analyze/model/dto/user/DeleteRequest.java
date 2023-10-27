package com.lk.analyze.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求体
 * @Author : lk
 * @create 2023/10/14
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private String id;

    private static final long serialVersionUID = 1L;
}
