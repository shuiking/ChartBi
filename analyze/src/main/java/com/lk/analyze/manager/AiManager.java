package com.lk.analyze.manager;

import com.lk.common.api.ErrorCode;
import com.lk.common.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ai控制器
 * @Author : lk
 * @create 2023/10/15
 */
@Service
public class AiManager {
    @Resource
    private YuCongMingClient client;

    public String doChat(String message,Long modelId){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        // 获取响应
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);

        if (response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 响应错误");
        }
        return response.getData().getContent();
    }
}
