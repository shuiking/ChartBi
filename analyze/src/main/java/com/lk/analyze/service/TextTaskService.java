package com.lk.analyze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lk.analyze.model.dto.text.GenTextTaskByAiRequest;
import com.lk.analyze.model.entity.TextTask;
import com.lk.common.model.to.UserTo;
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
    TextTask getTextTask(MultipartFile multipartFile, GenTextTaskByAiRequest genTextTaskByAiRequest, UserTo loginUser);

    /**
     * 文本更新失败
     * @param textTaskId
     * @param execMessage
     */
    void handleTextTaskUpdateError(Long textTaskId, String execMessage);


}
