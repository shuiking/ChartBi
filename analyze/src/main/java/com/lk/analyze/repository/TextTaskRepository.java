package com.lk.analyze.repository;

import com.lk.analyze.model.document.TextTask;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

/**
 * 文本任务表Repository
 * @Author : lk
 * @create 2023/10/27
 */
@Component
public interface TextTaskRepository extends MongoRepository<TextTask, String> {
    /**
     * 根据用户id获取当前用的全部图表数据
     */
    Page<TextTask> findTextTaskByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据文本任务id删除数据
     */
    int deleteTextTaskById(String id);

    /**
     * 根据文本任务id获取数据
     */
    TextTask findTextTaskById(String id);


}
