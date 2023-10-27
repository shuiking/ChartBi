package com.lk.analyze.repository;

import com.lk.analyze.model.dto.text.GenTextTaskByAiRequest;
import com.lk.analyze.model.entity.TextTask;
import com.lk.common.model.to.UserTo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文本任务表Repository
 * @Author : lk
 * @create 2023/10/27
 */
public interface TextTaskRepository {
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
    void handleTextTaskUpdateError(Long textTaskId, String execMessage);
}
