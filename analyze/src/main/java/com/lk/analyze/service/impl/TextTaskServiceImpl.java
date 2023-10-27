package com.lk.analyze.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.lk.analyze.constant.TextConstant;
import com.lk.analyze.model.document.TextRecord;
import com.lk.analyze.model.document.TextTask;
import com.lk.analyze.model.dto.text.GenTextTaskByAiRequest;
import com.lk.analyze.model.dto.text.TextAddRequest;
import com.lk.analyze.model.dto.text.TextUpdateRequest;
import com.lk.analyze.repository.TextTaskRepository;
import com.lk.analyze.service.TextRecordService;
import com.lk.analyze.service.TextTaskService;
import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import com.lk.common.exception.ThrowUtils;
import com.lk.common.feign.CreditFeignService;
import com.lk.common.model.to.UserTo;
import com.lk.common.utils.TxtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author k
 * @description 针对表【text_task(文本任务表)】的数据库操作Service实现
 * @createDate 2023-10-14 21:32:17
 */
@Service
@Slf4j
public class TextTaskServiceImpl implements TextTaskService{
    @DubboReference
    private CreditFeignService creditFeignService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private TextTaskRepository textTaskRepository;

    @Override
    public String addTextTask(Long userId, TextAddRequest textTaskAddRequest) {
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskAddRequest, textTask);
        textTask.setCreateTime(DateUtil.date());
        textTask.setUpdateTime(DateUtil.date());
        TextTask save = textTaskRepository.save(textTask);
        return save.getId();

    }

    @Override
    public Boolean deleteTextTaskById(UserTo user, String id) {
        // 判断是否存在
        Optional<TextTask> textTask = textTaskRepository.findById(id);
        ThrowUtils.throwIf(!textTask.isPresent(), ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可删除
        if (!textTask.get().getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int result = textTaskRepository.deleteTextTaskById(id);
        return result>0;
    }

    @Override
    public Boolean updateTextTaskById(TextUpdateRequest textTaskUpdateRequest,Long userId) {
        TextTask textTask = new TextTask();
        BeanUtils.copyProperties(textTaskUpdateRequest, textTask);
        String id = textTaskUpdateRequest.getId();
        // 判断是否存在
        Optional<TextTask> oldTextTask = textTaskRepository.findById(id);
        ThrowUtils.throwIf(!oldTextTask.isPresent(), ErrorCode.NOT_FOUND_ERROR);
        //判断为自己的文本
        ThrowUtils.throwIf(!userId.equals(oldTextTask.get().getUserId()),ErrorCode.OPERATION_ERROR);
        Update update = this.setUpdateData(textTask);
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(id)),update,TextTask.class);
        return true;
    }

    @Override
    public Boolean updateTextTask(TextTask textTask) {
        Update update = this.setUpdateData(textTask);
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(textTask.getId())),update,TextTask.class);
        return true;
    }

    @Override
    public TextTask getTextTaskById(String id) {
        return textTaskRepository.findTextTaskById(id);
    }

    @Override
    public Page<TextTask> getTextTaskListByUserId(Integer current, Integer size, Long userId) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        return textTaskRepository.findTextTaskByUserIdOrderByCreateTimeDesc(userId,pageRequest);
    }

    @Override
    public Boolean updateTextTaskStatusById(String id, String status) {
        Update update = new Update();
        update.set("status",status);
        update.set("updateTime",DateUtil.date());
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(id)), update, TextTask.class);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TextTask getTextTask(MultipartFile multipartFile, GenTextTaskByAiRequest genTextTaskByAiRequest, UserTo loginUser) {
        String textTaskType = genTextTaskByAiRequest.getTextType();
        String name = genTextTaskByAiRequest.getName();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(textTaskType), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>=100,ErrorCode.PARAMS_ERROR,"名称过长");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024*1024;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1MB");
        ThrowUtils.throwIf(size==0,ErrorCode.PARAMS_ERROR,"文件为空");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("txt");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀名非法");

        //消耗积分
        Boolean creditResult=creditFeignService.useCredit(loginUser.getId());
        ThrowUtils.throwIf(!creditResult,ErrorCode.OPERATION_ERROR,"你的积分不足");

        //保存数据库 wait
        //保存任务进数据库
        TextTask textTask = new TextTask();
        textTask.setTextType(textTaskType);
        textTask.setName(name);
        textTask.setUserId(loginUser.getId());
        textTask.setStatus(TextConstant.WAIT);
        textTask.setCreateTime(DateUtil.date());
        textTask.setUpdateTime(DateUtil.date());
        textTaskRepository.save(textTask);

        String taskId = textTask.getId();
        // 压缩后的数据
        ArrayList<String> textContentList = TxtUtils.readerFile(multipartFile);
        ThrowUtils.throwIf(textContentList.size() ==0,ErrorCode.PARAMS_ERROR,"文件为空");

        //将分割的内容保存入记录表
        ArrayList<TextRecord> taskArrayList = new ArrayList<>();
        textContentList.forEach(textContent ->{
            TextRecord textRecord = new TextRecord();
            textRecord.setTextTaskId(taskId);
            textRecord.setTextContent(textContent);
            textRecord.setStatus(TextConstant.WAIT);
            taskArrayList.add(textRecord);
        });
        mongoTemplate.insertAll(taskArrayList);
        return textTask;
    }

    @Override
    public void handleTextTaskUpdateError(String textTaskId, String execMessage) {
        TextTask updateTextTaskResult = new TextTask();
        updateTextTaskResult.setStatus(TextConstant.FAILED);
        updateTextTaskResult.setExecMessage(execMessage);
        Update update = new Update();
        update.set("status",TextConstant.FAILED);
        update.set("execMessage",execMessage);
        Boolean updateResult = this.updateTextTask(updateTextTaskResult);
        if (!updateResult){
            log.error("更新文本失败状态失败"+textTaskId+","+execMessage);
        }
    }

    @Override
    public Update setUpdateData(TextTask textTask) {
        Update update = new Update();
        if(textTask.getName()!=null)
            update.set("name",textTask.getName());
        if(textTask.getTextType()!=null)
            update.set("textType",textTask.getTextType());
        if(textTask.getGenTextContent()!=null)
            update.set("genTextContent",textTask.getGenTextContent());
        if(textTask.getStatus()!=null)
            update.set("status",textTask.getStatus());
        if(textTask.getExecMessage()!=null)
            update.set("execMessage",textTask.getExecMessage());
        update.set("updateTime", DateUtil.date());
        if(textTask.getIsDelete()!=null)
            update.set("isDelete",textTask.getIsDelete());
        return update;

    }
}




