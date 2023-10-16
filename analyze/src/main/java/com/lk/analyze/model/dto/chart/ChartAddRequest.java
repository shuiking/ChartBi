package com.lk.analyze.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图表创建请求
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class ChartAddRequest implements Serializable {
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表名称
     */
    private String name;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chatType;



    private static final long serialVersionUID = 1L;
}
