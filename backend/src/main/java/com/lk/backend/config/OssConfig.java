package com.lk.backend.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * minio配置参数
 * @Author : lk
 * @create 2023/10/23
 */
@Configuration
@Data
public class OssConfig {
    @Value("${cloud.oss.endpoint}")
    private String endpoint;
    @Value("${cloud.oss.bucket}")
    private String bucket;
    @Value("${cloud.oss.access-key}")
    private String accessKey;
    @Value("${cloud.oss.secret-key}")
    private String secretKey;
}
