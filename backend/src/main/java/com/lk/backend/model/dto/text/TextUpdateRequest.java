package com.lk.backend.model.dto.text;

import lombok.Data;

import java.io.Serializable;

/**
 * 笔记更新请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class TextUpdateRequest implements Serializable {

    /**
     * 任务id
     */
    private Long id;

    /**
     * 笔记名称
     */
    private String name;

    /**
     * 文本类型
     */
    private String textType;

    /**
     * 生成的文本内容
     */
    private String genTextContent;

    /**
     * wait,running,succeed,failed
     */
    private String status;

    /**
     * 执行信息
     */
    private String execMessage;



    private static final long serialVersionUID = 1L;
}
