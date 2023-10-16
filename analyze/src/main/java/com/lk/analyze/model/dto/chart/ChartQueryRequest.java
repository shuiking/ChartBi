package com.lk.analyze.model.dto.chart;

import com.lk.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 图表查询请求体
 * @Author : lk
 * @create 2023/10/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;


    /**
     * 图表类型
     */
    private String chatType;

    /**
     * 创建用户Id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
