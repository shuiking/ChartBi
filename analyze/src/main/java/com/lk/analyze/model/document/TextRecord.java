package com.lk.analyze.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 文本记录
 * @Author : lk
 * @create 2023/10/26
 */
@Document("text_record")
@Data
public class TextRecord {
    /**
     * id
     */
    @Id
    private String id;

    /**
     * 文本记录id
     */
    @Indexed
    private Long recordId;

    /**
     * 文本任务id
     */
    private Long textTaskId;

    /**
     * 文本内容
     */
    private String textContent;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}
