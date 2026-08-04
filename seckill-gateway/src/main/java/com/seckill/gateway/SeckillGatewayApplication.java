package com.seckill.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 秒杀网关启动类
 */
@SpringBootApplication(scanBasePackages = {"com.seckill"})
@EnableZuulProxy
@EnableDiscoveryClient
@EnableFeignClients
public class SeckillGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SeckillGatewayApplication.class, args);
    }
}
