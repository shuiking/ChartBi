package com.lk.analyze.mq.bi;

import cn.hutool.core.date.DateTime;
import com.lk.analyze.manager.AiManager;
import com.lk.analyze.model.document.Chart;
import com.lk.analyze.service.ChartService;
import com.lk.analyze.constant.ChartConstant;
import com.lk.analyze.constant.MqConstant;
import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * 图表分析消费者队列
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {MqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String chartId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.warn("接收到队列信息，receiveMessage={}=======================================",chartId);
        if (StringUtils.isBlank(chartId)){
            //消息为空，消息拒绝，不重复发送，不重新放入队列
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        Chart chart=chartService.getChartById(chartId);
        if (chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表为空");
        }

        //修改表状态为执行中，执行成功修改为“已完成”；执行失败修改为“失败”
        Boolean updateResult = chartService.updateChartStatusById(chart.getId(), ChartConstant.RUNNING);
        if (!updateResult){
            handleChartUpdateError(chart.getId(),"更新图表执行状态失败");
            return;
        }
        //调用AI
        String result = null;
        try {
            result = aiManager.doChat(chartService.buildUserInput(chart).toString(), ChartConstant.MODE_ID);
        } catch (Exception e) {
            channel.basicNack(deliveryTag,false,true);
            log.warn("信息放入队列{}", DateTime.now());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 服务错误");
        }
        //处理返回的数据
        try {
            boolean saveResult = chartService.saveChartAiResult(result, chart.getId());
            if (!saveResult){
                chartService.handleChartUpdateError(chart.getId(), "图表数据保存失败");
            }
        } catch (Exception e) {
            //重新放回队列
            channel.basicNack(deliveryTag,false,true);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表数据保存失败");
        }
        //消息确认
        channel.basicAck(deliveryTag,false);
    }
    private void handleChartUpdateError(String chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setStatus(ChartConstant.FAILED);
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
//        TODO 更新问题
        boolean updateResult = chartService.updateChart(updateChartResult);
        if (!updateResult){
            log.error("更新图片失败状态失败"+chartId+","+execMessage);
        }
    }
}
