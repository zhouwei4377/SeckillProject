package com.seckill.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 秒杀定时任务服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.seckill"})
@EnableDiscoveryClient
@EnableFeignClients
public class SeckillJobApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SeckillJobApplication.class, args);
    }
}
