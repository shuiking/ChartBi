package com.lk.analyze.service;

import com.lk.analyze.model.document.Chart;
import com.lk.analyze.model.dto.chart.ChartAddRequest;
import com.lk.analyze.model.dto.chart.ChartUpdateRequest;
import com.lk.analyze.model.dto.chart.GenChartByAiRequest;
import com.lk.common.model.to.UserTo;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author k
 * @description 针对表【chart(图表信息表)】的数据库操作Service
 * @createDate 2023-10-14 21:32:17
 */
public interface ChartService{
    /**
     * 创建图表
     * @param chartAddRequest
     * @param user
     * @return
     */
    String addChart(ChartAddRequest chartAddRequest,UserTo user);

    /**
     * 根据id删除图表
     * @param id
     * @param user
     * @return
     */
    Boolean deleteChartById(String id,UserTo user);

    /**
     * 根据id更新图表
     * @param chartUpdateRequest
     * @return
     */
    Boolean updateChartById(ChartUpdateRequest chartUpdateRequest);

    /**
     * 根据id获取图表
     * @param id
     * @return
     */
    Chart getChartById(String id);

    /**
     * 根据用户id获取分页的图表
     * @param current
     * @param size
     * @param userId
     * @return
     */
    Page<Chart> getChartListByUserId(Integer current,Integer size,Long userId);

    /**
     * 根据图表id更新图表的状态
     * @param id
     * @param status
     * @return
     */
    Boolean updateChartStatusById(String id,String status);

    Boolean updateChart(Chart chart);


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
    boolean saveChartAiResult(String result, String chartId);

    /**
     * 图表更新失败
     * @param chartId
     * @param execMessage
     */

    void handleChartUpdateError(String chartId, String execMessage);
    /**
     * 获取准备分析的表数据(事务回滚)
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     */
    Chart getChartTask(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, UserTo loginUser);

    /**
     * 图表的更新信息
     * @param chart
     * @return
     */
    Update setUpdateData(Chart chart);
}
