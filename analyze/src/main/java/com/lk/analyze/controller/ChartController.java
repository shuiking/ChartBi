package com.lk.analyze.controller;

import com.lk.analyze.interceptor.LoginUserInterceptor;
import com.lk.analyze.manager.AiManager;
import com.lk.analyze.manager.RedisLimiterManager;
import com.lk.analyze.model.document.Chart;
import com.lk.analyze.model.dto.chart.ChartAddRequest;
import com.lk.analyze.mq.common.MqMessageProducer;
import com.lk.analyze.service.ChartService;
import com.lk.analyze.annotation.AuthCheck;
import com.lk.analyze.constant.ChartConstant;
import com.lk.analyze.constant.MqConstant;
import com.lk.analyze.model.dto.chart.*;
import com.lk.analyze.model.dto.user.DeleteRequest;
import com.lk.analyze.model.vo.AiResponseVo;
import com.lk.analyze.model.vo.ChartVo;
import com.lk.common.api.BaseResponse;
import com.lk.common.api.ErrorCode;
import com.lk.common.api.ResultUtils;
import com.lk.common.constant.UserConstant;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 图表分析接口
 * @Author : lk
 * @create 2023/10/15
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private AiManager aiManager;
    @Resource
    ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private MqMessageProducer mqMessageProducer;


    /**
     * 创建图表
     * @param chartAddRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<String> addChart(@RequestBody ChartAddRequest chartAddRequest) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        String newChartId = chartService.addChart(chartAddRequest,loginUser);
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除图表
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        boolean result=chartService.deleteChartById(deleteRequest.getId(),loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 更新（仅管理员）
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() ==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result=chartService.updateChartById(chartUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(String id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getChartById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 根据 id 获取 图表脱敏
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartVo> getChartVOById(String id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getChartById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        ChartVo chartVo = new ChartVo();
        BeanUtils.copyProperties(chart,chartVo);
        return ResultUtils.success(chartVo);
    }

    /**
     * 分页获取列表（封装类）
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        Page<Chart> chartPage = chartService.getChartListByUserId((int)current,(int)size,loginUser.getId());
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest){
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.getChartListByUserId((int)current,(int)size,loginUser.getId());

        return ResultUtils.success(chartPage);
    }


    /**
     * 图表数据上传(同步)
     * @param multipartFile
     * @param genChartByAiRequest
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<AiResponseVo> genChartAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest) {

        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());
        //获取任务表数据
        Chart chartTask = chartService.getChartTask(multipartFile, genChartByAiRequest, loginUser);

        String result = aiManager.doChat(chartService.buildUserInput(chartTask), ChartConstant.MODE_ID);
        //处理返回的数据
        boolean saveResult = chartService.saveChartAiResult(result, chartTask.getId());
        if (!saveResult){
            chartService.handleChartUpdateError(chartTask.getId(), "图表数据保存失败");
        }
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(chartTask.getId());
        return ResultUtils.success(aiResponse);
    }

    /**
     * 图表数据上传(异步)
     * @param multipartFile
     * @param genChartByAiRequest
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<AiResponseVo> genChartAsyncAi(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest) {

        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());
        //获取任务表数据
        Chart chartTask = chartService.getChartTask(multipartFile, genChartByAiRequest, loginUser);

        //todo 需要处理队列满后的异常
        try {
            CompletableFuture.runAsync(()->{
                Boolean updateResult = chartService.updateChartStatusById(chartTask.getId(), ChartConstant.RUNNING);
                if (!updateResult){
                    chartService.handleChartUpdateError(chartTask.getId(),"更新图表执行状态失败");
                    return;
                }
                //调用AI
                String result = aiManager.doChat(chartService.buildUserInput(chartTask),ChartConstant.MODE_ID);
                //处理返回的数据
                boolean saveResult = chartService.saveChartAiResult(result, chartTask.getId());
                if (!saveResult){
                    chartService.handleChartUpdateError(chartTask.getId(), "图表数据保存失败");
                }
            },threadPoolExecutor);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统繁忙，请稍后重试");
        }
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(chartTask.getId());
        return ResultUtils.success(aiResponse);
    }

    /**
     * 图表数据上传(mq)
     * @param multipartFile
     * @param genChartByAiRequest
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<AiResponseVo> genChartAsyncAiMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest) {
        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());
        //获取任务表数据
        Chart chartTask = chartService.getChartTask(multipartFile, genChartByAiRequest, loginUser);

        String chartId = chartTask.getId();
        log.warn("准备发送信息给队列，Message={}=======================================",chartId);
        mqMessageProducer.sendMessage(MqConstant.BI_EXCHANGE_NAME,MqConstant.BI_ROUTING_KEY,String.valueOf(chartId));
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(chartTask.getId());
        return ResultUtils.success(aiResponse);
    }


    /**
     * 图表重新生成(mq)
     * @param chartRebuildRequest
     * @return
     */
    @PostMapping("/gen/async/rebuild")
    public BaseResponse<AiResponseVo> genChartAsyncAiRebuild(ChartRebuildRequest chartRebuildRequest) {
        String chartId = chartRebuildRequest.getId();
        Chart genChartByAiRequest=chartService.getChartById(chartId);
        String chartType = genChartByAiRequest.getChatType();
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartData = genChartByAiRequest.getChartData();

        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>=100,ErrorCode.PARAMS_ERROR,"名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(chartData),ErrorCode.PARAMS_ERROR,"表格数据为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType),ErrorCode.PARAMS_ERROR,"生成表格类型为空");

        UserTo loginUser = LoginUserInterceptor.loginUser.get();
        //限流
        redisLimiterManager.doRateLimit("doRateLimit_" + loginUser.getId());

        //更改状态 wait
        boolean saveResult=chartService.updateChartStatusById(chartId,ChartConstant.WAIT);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"图表保存失败");
        log.warn("准备发送信息给队列，Message={}=======================================",chartId);
        mqMessageProducer.sendMessage(MqConstant.BI_EXCHANGE_NAME, MqConstant.BI_ROUTING_KEY,String.valueOf(chartId));
        //返回数据参数
        AiResponseVo aiResponse = new AiResponseVo();
        aiResponse.setResultId(chartId);
        return ResultUtils.success(aiResponse);

    }
}
