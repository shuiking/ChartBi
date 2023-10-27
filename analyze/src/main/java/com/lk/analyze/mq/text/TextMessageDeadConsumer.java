package com.lk.analyze.mq.text;

import com.lk.analyze.constant.MqConstant;
import com.lk.analyze.constant.TextConstant;
import com.lk.analyze.model.document.TextTask;
import com.lk.analyze.service.TextTaskService;
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
 * 文本转换队列的死信队列
 */
@Component
@Slf4j
public class TextMessageDeadConsumer {
    @Resource
    private TextTaskService textTaskService;


    @SneakyThrows
    @RabbitListener(queues = {MqConstant.TEXT_DEAD_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String textTaskId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.warn("接收到死信队列信息，receiveMessage={}=======================================",textTaskId);
        if (StringUtils.isBlank(textTaskId)){
            //消息为空，消息拒绝，不重复发送，不重新放入队列
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        TextTask textTask = textTaskService.getTextTaskById(textTaskId);
        if (textTask == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文本为空");
        }

        //修改表状态为执行中，执行成功修改为“已完成”；执行失败修改为“失败”
        boolean updateResult = textTaskService.updateTextTaskStatusById(textTask.getId(),TextConstant.FAILED);
        //这里不对记录表状态修改，记录只能内部使用
        if (!updateResult){
            textTaskService.handleTextTaskUpdateError(textTask.getId(),"更新图表执行状态失败");
            return;
        }
        //消息确认
        channel.basicAck(deliveryTag,false);
    }
}
