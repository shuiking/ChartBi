package com.lk.analyze.controller;

import com.lk.analyze.constant.MqConstant;
import com.lk.analyze.constant.TextConstant;
import com.lk.analyze.interceptor.LoginUserInterceptor;
import com.lk.analyze.manager.AiManager;
import com.lk.analyze.manager.RedisLimiterManager;
import com.lk.analyze.model.document.TextRecord;
import com.lk.analyze.model.document.TextTask;
import com.lk.analyze.model.dto.text.*;
import com.lk.analyze.model.dto.user.DeleteRequest;
import com.lk.analyze.model.vo.AiResponseVo;
import com.lk.analyze.model.vo.TextTaskVo;
import com.lk.analyze.mq.common.MqMessageProducer;
import com.lk.analyze.service.TextRecordService;
import com.lk.analyze.service.TextTaskService;
import com.lk.common.api.BaseResponse;
import com.lk.common.api.ErrorCode;
import com.lk.common.api.ResultUtils;
import com.lk.common.exception.BusinessException;
import com.lk.common.exception.ThrowUtils;
import com.lk.common.model.to.UserTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * 笔记转化接口
 * @Author : lk
 * @create 2023/10/15
 */
@RestController
@RequestMapping("/text")
@Slf4j
public class TextController {
    @Resource
    private TextTaskService textTaskService;

    @Resource
    private TextRecordService textRecordService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private AiManager aiManager;

    @Resource
    private MqMessageProducer mqMessageProducer;

    /**
     * 创建
     * @param textTaskAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addTextTask(@RequestBody TextAddRequest textTaskAddRequest) {
        if (textTaskAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        String newTextTaskId = textTaskService.addTextTask(loginUser.getId(),textTaskAddRequest);
        return ResultUtils.success(newTextTaskId);
    }

    /**
     * 删除
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTextTask(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo user = LoginUserInterceptor.loginUser.get();
        boolean result = textTaskService.deleteTextTaskById(user,deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新自己文本
     * @param textTaskUpdateRequest
     * @return
     */
    @PostMapping("/my/update")
    public BaseResponse<Boolean> updateMyTextTask(@RequestBody TextUpdateRequest textTaskUpdateRequest) {
        if (textTaskUpdateRequest == null || textTaskUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo user = LoginUserInterceptor.loginUser.get();
        boolean result = textTaskService.updateTextTaskById(textTaskUpdateRequest,user.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TextTask> getTextTaskById(String id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = textTaskService.getTextTaskById(id);
        if (textTask == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(textTask);
    }

    /**
     * 根据 id 获取 图表脱敏
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<TextTaskVo> getTextTaskVOById(String id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TextTask textTask = textTaskService.getTextTaskById(id);
        if (textTask == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        TextTaskVo textTaskVo = new TextTaskVo();
        BeanUtils.copyProperties(textTask,textTaskVo);
        return ResultUtils.success(textTaskVo);
    }


    /**
     * 分页获取当前用户创建的资源列表
     * @param textTaskQueryRequest
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<TextTask>> listMyTextTaskByPage(@RequestBody TextTaskQueryRequest textTaskQueryRequest) {
        if (textTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        long current = textTaskQueryRequest.getCurrent();
        long size = textTaskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<TextTask> textTaskPage=textTaskService.getTextTaskListByUserId((int)current,(int)size,loginUser.getId());
        return ResultUtils.success(textTaskPage);
    }

    /**
     * 文本数据上传(同步)
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<AiResponseVo> genTextTaskAi(@RequestPart("file") MultipartFile multipartFile,
                                                    GenTextTaskByAiRequest genTextTaskByAiRequest) {

        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());
        //获取文本任务并校验
        TextTask textTask = textTaskService.getTextTask(multipartFile, genTextTaskByAiRequest, loginUser);

        //获取任务id
        String taskId = textTask.getId();
        String textType = textTask.getTextType();
        //从根据任务id记录表中获取数据
        List<TextRecord> textRecords = textRecordService.getTextRecordListByTextTaskId(taskId);

        //将文本依次交给ai处理
        for (TextRecord textRecord : textRecords) {
            String result = null;
            result = aiManager.doChat(textRecordService.buildUserInput(textRecord,textType).toString(), TextConstant.MODE_ID);
            textRecord.setGenTextContent(result);
            textRecord.setStatus(TextConstant.SUCCEED);
            boolean updateById = textRecordService.updateTextRecordStatusById(textRecord);
            if (!updateById){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ai返回结果保存失败");
            }
        }


        //将记录表中已经生成好的内容合并存入任务表
        List<TextRecord> textRecord = textRecordService.getTextRecordListByTextTaskId(taskId);
        StringBuilder stringBuilder = new StringBuilder();
        textRecord.forEach(textRecord1 -> {
            stringBuilder.append(textRecord1.getGenTextContent()).append('\n');
        });
        TextTask textTask1 = new TextTask();
        textTask1.setId(taskId);
        textTask1.setGenTextContent(stringBuilder.toString());
        textTask1.setStatus(TextConstant.SUCCEED);
        boolean save = textTaskService.updateTextTask(textTask1);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"ai返回文本任务保存失败");
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(textTask.getId());
        return ResultUtils.success(aiResponse);

    }

    /**
     * 文本数据上传(mq)
     * @param multipartFile
     * @param genTextTaskByAiRequest
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<AiResponseVo> genTextTaskAsyncAiMq(@RequestPart("file") MultipartFile multipartFile,
                                                           GenTextTaskByAiRequest genTextTaskByAiRequest) {
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());
        //获取文本任务并校验
        TextTask textTask = textTaskService.getTextTask(multipartFile, genTextTaskByAiRequest, loginUser);

        //获取任务id
        String taskId = textTask.getId();
        log.warn("准备发送信息给队列，Message={}=======================================",taskId);
        mqMessageProducer.sendMessage(MqConstant.TEXT_EXCHANGE_NAME,MqConstant.TEXT_ROUTING_KEY,String.valueOf(taskId));
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(textTask.getId());
        return ResultUtils.success(aiResponse);
    }

    /**
     * 文本重新生成(mq)
     * @param textRebuildRequest
     * @return
     */
    @PostMapping("/gen/async/rebuild")
    public BaseResponse<AiResponseVo> genTextTaskAsyncAiRebuild(TextRebuildRequest textRebuildRequest) {
        String textTaskId = textRebuildRequest.getId();
        //获取记录表
        List<TextRecord> recordList =textRecordService.getTextRecordListByTextTaskId(textTaskId);
        //校验，查看原始文本是否为空
        recordList.forEach(textRecord -> {
            ThrowUtils.throwIf(StringUtils.isBlank(textRecord.getTextContent()),ErrorCode.PARAMS_ERROR,"文本为空");
        });

        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());

        //更新状态 wait
        boolean saveResult = textTaskService.updateTextTaskStatusById(textTaskId,TextConstant.WAIT);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"文本保存失败");
        log.warn("准备发送信息给队列，Message={}=======================================",textTaskId);
        mqMessageProducer.sendMessage(MqConstant.TEXT_EXCHANGE_NAME,MqConstant.TEXT_ROUTING_KEY,String.valueOf(textTaskId));
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(textTaskId);
        return ResultUtils.success(aiResponse);

    }
}
