package com.lk.analyze.repository;

import com.lk.analyze.model.document.Chart;
import com.lk.analyze.model.dto.chart.GenChartByAiRequest;
import com.lk.common.model.to.UserTo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图表Repository
 * @Author : lk
 * @create 2023/10/27
 */
public interface ChartRepository extends MongoRepository<Chart,String> {
    /**
     * 图表用户输入构造
     * @param chart
     * @return
     */
    String buildUserInput(com.lk.analyze.model.entity.Chart chart);
    /**
     * 处理Ai返回信息保存
     * @param result
     * @return
     */
    boolean saveChartAiResult(String result, long chartId);

    /**
     * 图表更新失败
     * @param chartId
     * @param execMessage
     */
    void handleChartUpdateError(Long chartId, String execMessage);
    /**
     * 获取准备分析的表数据(事务回滚)
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     */
    com.lk.analyze.model.entity.Chart getChartTask(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, UserTo loginUser);
}
