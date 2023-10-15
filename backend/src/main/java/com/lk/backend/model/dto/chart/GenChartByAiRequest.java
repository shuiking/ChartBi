package com.lk.backend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图表生成请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class GenChartByAiRequest implements Serializable {
    /**
     * 图表名称
     */
    private String name;
    /**
     * 图表描述
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;


    private static final long serialVersionUID = 1L;
}
