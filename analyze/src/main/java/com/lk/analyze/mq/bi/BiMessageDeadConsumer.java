package com.lk.analyze.mq.bi;

import com.lk.analyze.constant.ChartConstant;
import com.lk.analyze.constant.MqConstant;
import com.lk.analyze.model.document.Chart;
import com.lk.analyze.service.ChartService;
import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 图表分析队列的死信队列
 */
@Component
@Slf4j
public class BiMessageDeadConsumer {

    @Resource
    private ChartService chartService;
    @Resource
    private MongoTemplate mongoTemplate;


    @SneakyThrows
    @RabbitListener(queues = {MqConstant.BI_DEAD_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String chartId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.warn("接收到死信队列信息，receiveMessage={}=======================================",chartId);
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
        boolean updateResult = chartService.updateChartStatusById(chartId, ChartConstant.FAILED);
        if (!updateResult){
            handleChartUpdateError(chart.getId(),"更新图表执行状态失败");
            return;
        }
        //消息确认
        channel.basicAck(deliveryTag,false);
    }
    private void handleChartUpdateError(String chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setStatus(ChartConstant.FAILED);
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateChart(updateChartResult);
        if (!updateResult){
            log.error("更新图片失败状态失败"+chartId+","+execMessage);
        }
    }
}
