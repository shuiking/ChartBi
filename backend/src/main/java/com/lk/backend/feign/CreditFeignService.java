package com.lk.backend.feign;

/**
 * @Author : lk
 * @create 2023/10/21
 */
public interface CreditFeignService {
    Boolean useCredit(Long userId);
}
