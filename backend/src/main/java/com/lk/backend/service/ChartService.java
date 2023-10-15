package com.lk.backend.service;

import com.lk.backend.model.dto.chart.GenChartByAiRequest;
import com.lk.backend.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.backend.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
* @author k
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-10-14 21:32:17
*/
public interface ChartService extends IService<Chart> {
    /**
     * 图表用户输入构造
     * @param chart
     * @return
     */
    String buildUserInput(Chart chart);
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
    Chart getChartTask(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser);
}
