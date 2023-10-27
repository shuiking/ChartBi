package com.lk.analyze.repository;

import com.lk.analyze.model.entity.TextRecord;

/**
 * 文本记录表Repository
 * @Author : lk
 * @create 2023/10/27
 */
public interface TextRecordRepository {
    /**
     * 文本用户输入构造
     * @param textRecord
     * @param textTaskType
     * @return
     */
    String buildUserInput(TextRecord textRecord, String textTaskType);
}
