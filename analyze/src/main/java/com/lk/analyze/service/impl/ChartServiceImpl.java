package com.lk.analyze.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.lk.analyze.constant.ChartConstant;
import com.lk.analyze.model.document.Chart;
import com.lk.analyze.model.dto.chart.ChartAddRequest;
import com.lk.analyze.model.dto.chart.ChartUpdateRequest;
import com.lk.analyze.model.dto.chart.GenChartByAiRequest;
import com.lk.analyze.repository.ChartRepository;
import com.lk.analyze.service.ChartService;
import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import com.lk.common.exception.ThrowUtils;
import com.lk.common.feign.CreditFeignService;
import com.lk.common.model.to.UserTo;
import com.lk.common.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author k
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-10-14 21:32:17
 */
@Service
@Slf4j
public class ChartServiceImpl implements ChartService{
    @DubboReference
    private CreditFeignService creditFeignService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ChartRepository chartRepository;


    @Override
    public String addChart(ChartAddRequest chartAddRequest, UserTo user) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        chart.setUserId(user.getId());
        chart.setCreateTime(DateUtil.date());
        chart.setUpdateTime(DateUtil.date());
        Chart save = chartRepository.save(chart);
//        TODO 返回id问题
        return save.getId();
    }

    @Override
    public Boolean deleteChartById(String id,UserTo user) {
        // 判断图表是否存在
        Optional<Chart> result = chartRepository.findById(id);
        // 要更新的图表不存在
        if(!result.isPresent()) return false;
        // 仅本人或管理员可删除
        if (!result.get().getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 删除图表
        int byId = chartRepository.deleteChartById(id);
        return byId > 0;
    }

    @Override
    public Boolean updateChartById(ChartUpdateRequest chartUpdateRequest) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 判断是否存在
        Optional<Chart> result = chartRepository.findById(chart.getId());
        if(result.isPresent()){
            Update update = this.setUpdateData(chart);
            mongoTemplate.updateFirst(new Query(Criteria.where("id").is(chart.getId())),update,Chart.class);
            return true;
        }
        return false;
    }

    @Override
    public Chart getChartById(String id) {
        return chartRepository.findChartById(id);
    }

    @Override
    public Page<Chart> getChartListByUserId(Integer current, Integer size, Long userId) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        return chartRepository.findChartByUserIdOrderByCreateTimeDesc(userId,pageRequest);
    }

    @Override
    public Boolean updateChartStatusById(String id, String status) {
        Update update = new Update();
        update.set("status",status);
        update.set("updateTime",DateUtil.date());
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(id)), update, Chart.class);
        return true;
    }

    @Override
    public Boolean updateChart(Chart chart) {
        Update update = this.setUpdateData(chart);
        mongoTemplate.updateFirst(new Query(Criteria.where("id").is(chart.getId())), update, Chart.class);
        return true;
    }

    @Override
    public String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chatType = chart.getChatType();
        String csvData = chart.getChartData();
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chatType)) {
            userGoal += "，请使用" + chatType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Chart getChartTask(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, UserTo loginUser) {
        String chartType = genChartByAiRequest.getChartType();
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>=100,ErrorCode.PARAMS_ERROR,"名称不规范");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB = 1024*1024;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1MB");
        ThrowUtils.throwIf(size==0,ErrorCode.PARAMS_ERROR,"文件为空");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix = Arrays.asList("png","xlsx","svg","webp","jpeg");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀名非法");

        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        //消耗积分
        Boolean creditResult=creditFeignService.useCredit(loginUser.getId());
        ThrowUtils.throwIf(!creditResult,ErrorCode.OPERATION_ERROR,"你的积分不足");
        //保存数据库 wait
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartData(csvData);
        chart.setChatType(chartType);
        chart.setStatus(ChartConstant.WAIT);
        chart.setName(name);
        chart.setGoal(goal);
        chart.setCreateTime(DateUtil.date());
        chart.setUpdateTime(DateUtil.date());
        return chartRepository.save(chart);
    }

    @Override
    public Update setUpdateData(Chart chart) {
        Update update = new Update();
        if(chart.getName()!=null)
            update.set("name",chart.getName());
        if(chart.getGoal()!=null)
            update.set("goal",chart.getGoal());
        if(chart.getChartData()!=null)
            update.set("chartData",chart.getChartData());
        if(chart.getChatType()!=null)
            update.set("chatType",chart.getChatType());
        if(chart.getGenChat()!=null)
            update.set("genChat",chart.getGenChat());
        if(chart.getGenChat()!=null)
            update.set("genResult",chart.getGenChat());
        if(chart.getUserId()!=null)
            update.set("userId",chart.getUserId());
        if(chart.getStatus()!=null)
            update.set("status",chart.getStatus());
        if(chart.getExecMessage()!=null)
            update.set("execMessage",chart.getExecMessage());
        update.set("updateTime",DateUtil.date());
        if(chart.getIsDelete()!=null)
            update.set("isDelete",chart.getIsDelete());
        return update;
    }

    @Override
    public boolean saveChartAiResult(String result, String chartId) {
        String[] splits = result.split("【【【【【");

        if (splits.length < 3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 生成错误");
        }
        //todo 可以使用正则表达式保证数据准确性，防止中文出现
        String genChart= splits[1].trim();
        String genResult = splits[2].trim();
        //将非js格式转化为js格式
        try {
            HashMap<String,Object> genChartJson = JSONUtil.toBean(genChart, HashMap.class);
            genChart = JSONUtil.toJsonStr(genChartJson);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成图片错误");
        }
        //保存数据库
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus(ChartConstant.SUCCEED);
        updateChartResult.setGenChat(genChart);
        updateChartResult.setGenResult(genResult);
        return this.updateChart(updateChartResult);

    }

    @Override
    public void handleChartUpdateError(String chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setStatus(ChartConstant.FAILED);
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
        Boolean updateResult = this.updateChart(updateChartResult);
        if (!updateResult){
            log.error("更新图片失败状态失败"+chartId+","+execMessage);
        }
    }
}




