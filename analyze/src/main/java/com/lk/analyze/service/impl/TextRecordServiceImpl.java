package com.lk.analyze.service.impl;

import cn.hutool.core.date.DateUtil;
import com.lk.analyze.model.document.TextRecord;
import com.lk.analyze.model.document.TextTask;
import com.lk.analyze.repository.TextRecordRepository;
import com.lk.analyze.service.TextRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author k
 * @description 针对表【text_record(文本记录表)】的数据库操作Service实现
 * @createDate 2023-10-14 21:32:17
 */
@Service
public class TextRecordServiceImpl implements TextRecordService{
    @Resource
    private TextRecordRepository textRecordRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Override
    public String buildUserInput(TextRecord textRecord, String textTaskType) {
        String textContent = textRecord.getTextContent();
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        String gold = "请使用"+textTaskType+"语法对下面文章格式化";

        userInput.append(gold).append("\n");

        if (StringUtils.isNotBlank(textContent)) {
            textContent = textContent.trim();
            userInput.append(textContent);
        }
        return userInput.toString();
    }

    @Override
    public List<TextRecord> getTextRecordListByTextTaskId(String textTaskId) {
        return textRecordRepository.findTextRecordsByTextTaskId(textTaskId);
    }

    @Override
    public Boolean updateTextRecordStatusById(TextRecord textRecord) {
        Update update = new Update();
        update.set("genTextContent",textRecord.getGenTextContent());
        update.set("status",textRecord.getStatus());
        update.set("updateTime", DateUtil.date());
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(textRecord.getId())), update, TextRecord.class);
        return true;
    }

}




