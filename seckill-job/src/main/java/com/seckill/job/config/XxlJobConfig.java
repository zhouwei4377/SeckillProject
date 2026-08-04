package com.seckill.job.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job配置类
 * 配置XXL-Job执行器和管理中心连接
 */
@Slf4j
@Configuration
public class XxlJobConfig {
    
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    
    @Value("${xxl.job.executor.appname}")
    private String appName;
    
    @Value("${xxl.job.executor.ip}")
    private String executorIp;
    
    @Value("${xxl.job.executor.port}")
    private int executorPort;
    
    @Value("${xxl.job.accessToken}")
    private String accessToken;
    
    @Value("${xxl.job.executor.logpath}")
    private String logPath;
    
    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;
    
    /**
     * 配置XXL-Job执行器
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("初始化XXL-Job执行器...");
        
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setIp(executorIp);
        xxlJobSpringExecutor.setPort(executorPort);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        
        log.info("XXL-Job执行器初始化完成: appname={}, port={}", appName, executorPort);
        
        return xxlJobSpringExecutor;
    }
}
