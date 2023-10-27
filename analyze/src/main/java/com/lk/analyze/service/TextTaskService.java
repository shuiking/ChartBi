package com.lk.analyze.service;

import com.lk.analyze.model.document.Chart;
import com.lk.analyze.model.document.TextTask;
import com.lk.analyze.model.dto.chart.ChartUpdateRequest;
import com.lk.analyze.model.dto.text.GenTextTaskByAiRequest;
import com.lk.analyze.model.dto.text.TextAddRequest;
import com.lk.analyze.model.dto.text.TextUpdateRequest;
import com.lk.common.model.to.UserTo;
import javafx.concurrent.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author k
 * @description 针对表【text_task(文本任务表)】的数据库操作Service
 * @createDate 2023-10-14 21:32:17
 */
public interface TextTaskService{
    /**
     * 添加文本任务
     * @param userId
     * @param textTaskAddRequest
     * @return
     */
    String addTextTask(Long userId, TextAddRequest textTaskAddRequest);

    /**
     * 根据id删除文本任务
     * @param user
     * @param id
     * @return
     */
    Boolean deleteTextTaskById(UserTo user,String id);

    /**
     * 根据id更新文本任务
     * @param textTaskUpdateRequest
     * @param id
     * @return
     */
    Boolean updateTextTaskById(TextUpdateRequest textTaskUpdateRequest,Long userId);

    Boolean updateTextTask(TextTask textTask);

    /**
     * 根据id获取文本任务数据
     * @param id
     * @return
     */
    TextTask getTextTaskById(String id);

    /**
     * 根据用户id获取分页的文本任务
     * @param current
     * @param size
     * @param userId
     * @return
     */
    Page<TextTask> getTextTaskListByUserId(Integer current, Integer size, Long userId);

    /**
     * 根据文本任务id更新文本任务的状态
     * @param id
     * @param status
     * @return
     */
    Boolean updateTextTaskStatusById(String id,String status);

    /**
     * 获取准备分析的表数据(事务回滚)
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @param loginUser
     * @return
     */
    TextTask getTextTask(MultipartFile multipartFile, GenTextTaskByAiRequest genTextTaskByAiRequest, UserTo loginUser);

    /**
     * 文本更新失败
     * @param textTaskId
     * @param execMessage
     */
    void handleTextTaskUpdateError(String textTaskId, String execMessage);

    /**
     * 文本任务的更新信息
     * @param chart
     * @return
     */
    Update setUpdateData(TextTask textTask);


}
