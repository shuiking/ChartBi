package com.lk.backend.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class UploadFileRequest implements Serializable {
    /**
     * 业务
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}
