package com.lk.analyze;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableDubbo
@EnableRedisHttpSession
@MapperScan("com.lk.analyze.mapper")
@SpringBootApplication
public class AnalyzeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzeApplication.class, args);
    }

}
