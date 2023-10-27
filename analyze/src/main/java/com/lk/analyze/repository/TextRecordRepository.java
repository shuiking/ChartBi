package com.lk.analyze.repository;

import com.lk.analyze.model.document.TextRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文本记录Repository
 * @Author : lk
 * @create 2023/10/28
 */
@Component
public interface TextRecordRepository extends MongoRepository<TextRecord,String> {
    /**
     * 根据文本任务id获取文本记录数据
     */
    List<TextRecord> findTextRecordsByTextTaskId(String textTaskId);
}
