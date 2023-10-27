package com.lk.analyze.repository;

import com.lk.analyze.model.document.Chart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 图表Repository
 * @Author : lk
 * @create 2023/10/27
 */
@Component
public interface ChartRepository extends MongoRepository<Chart,String> {
    /**
     * 获取当前用的全部图表数据
     */
    Page<Chart> findChartByUserIdOrderByCreateTimeDesc(Long userId,Pageable pageable);

    /**
     * 根据图表id删除数据
     */
    int deleteChartById(String id);

    /**
     * 根据图表id获取数据
     */
    Chart findChartById(String id);


}
