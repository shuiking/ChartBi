package com.lk.backend.service;

import com.lk.backend.model.entity.TextRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author k
* @description 针对表【text_record(文本记录表)】的数据库操作Service
* @createDate 2023-10-14 21:32:17
*/
public interface TextRecordService extends IService<TextRecord> {
    /**
     * 文本用户输入构造
     * @param textRecord
     * @param textTaskType
     * @return
     */
    String buildUserInput(TextRecord textRecord,String textTaskType);
}
