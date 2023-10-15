package com.lk.backend.model.dto.text;

import com.lk.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 笔记查询请求体
 * @Author : lk
 * @create 2023/10/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TextTaskQueryRequest extends PageRequest implements Serializable {

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
     * 创建用户Id
     */
    private Long userId;

    /**
     * wait,running,succeed,failed
     */
    private String status;

    private static final long serialVersionUID = 1L;
}
