package com.lk.backend.feign;

import com.lk.backend.constant.CreditConstant;
import com.lk.backend.service.CreditService;
import com.lk.common.feign.CreditFeignService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @Author : lk
 * @create 2023/10/21
 */
@DubboService
public class CreditFeignServiceImpl implements CreditFeignService {
    @Resource
    private CreditService creditService;
    @Override
    public Boolean useCredit(Long userId) {
        return creditService.updateCredits(userId, CreditConstant.CREDIT_CHART_SUCCESS);
    }
}
