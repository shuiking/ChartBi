package com.lk.common.feign;

/**
 * @Author : lk
 * @create 2023/10/26
 */
public interface CreditFeignService {
    Boolean useCredit(Long userId);
}
