package com.lk.analyze.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 图表重新请求请求体
 * @Author : lk
 * @create 2023/10/15
 */
@Data
public class ChartRebuildRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
