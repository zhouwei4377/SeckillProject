package com.seckill.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 秒杀订单服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.seckill"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement
@EnableScheduling
public class SeckillOrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SeckillOrderApplication.class, args);
    }
}
