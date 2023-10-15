package com.lk.backend.service;

import com.lk.backend.model.dto.text.GenTextTaskByAiRequest;
import com.lk.backend.model.entity.TextTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.backend.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
* @author k
* @description 针对表【text_task(文本任务表)】的数据库操作Service
* @createDate 2023-10-14 21:32:17
*/
public interface TextTaskService extends IService<TextTask> {
    /**
     * 获取准备分析的表数据(事务回滚)
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @param loginUser
     * @return
     */
    TextTask getTextTask(MultipartFile multipartFile, GenTextTaskByAiRequest genTextTaskByAiRequest, User loginUser);

    /**
     * 文本更新失败
     * @param textTaskId
     * @param execMessage
     */
    void handleTextTaskUpdateError(Long textTaskId, String execMessage);
}
