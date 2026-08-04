package com.seckill.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 支付服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.seckill"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableTransactionManagement
@EnableScheduling
public class SeckillPaymentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SeckillPaymentApplication.class, args);
    }
}
