package com.lk.analyze.service;


import com.lk.analyze.model.document.TextRecord;

import java.util.List;

/**
 * @author k
 * @description 针对表【text_record(文本记录表)】的数据库操作Service
 * @createDate 2023-10-14 21:32:17
 */
public interface TextRecordService{
    /**
     * 文本用户输入构造
     * @param textRecord
     * @param textTaskType
     * @return
     */
    String buildUserInput(TextRecord textRecord, String textTaskType);

    /**
     * 根据文本任务id获取文本记录数据
     * @param textTaskId
     * @return
     */
    List<TextRecord> getTextRecordListByTextTaskId(String textTaskId);

    /**
     * 根据文本记录id更新文本记录的状态
     * @param textRecord
     * @return
     */
    Boolean updateTextRecordStatusById(TextRecord textRecord);
}
