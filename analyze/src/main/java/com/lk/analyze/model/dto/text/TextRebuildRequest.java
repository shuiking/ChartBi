package com.lk.analyze.model.dto.text;

import lombok.Data;

import java.io.Serializable;

/**
 * 笔记重新生成请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class TextRebuildRequest implements Serializable {

    /**
     * id
     */
    private String id;

    private static final long serialVersionUID = 1L;
}

