package com.seckill.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID生成工具类
 * 生成订单ID、跟踪ID等业务ID
 */
public class IdGenerator {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    
    private static final int MAX_SEQUENCE = 10000;
    
    /**
     * 生成订单ID
     * 格式: ORDER-yyyyMMddHHmmss-sequenceNumber
     */
    public static String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % MAX_SEQUENCE;
        return String.format("ORDER-%s-%05d", timestamp, sequence);
    }
    
    /**
     * 生成支付ID
     * 格式: PAY-yyyyMMddHHmmss-sequenceNumber
     */
    public static String generatePaymentId() {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % MAX_SEQUENCE;
        return String.format("PAY-%s-%05d", timestamp, sequence);
    }
    
    /**
     * 生成追踪ID (Trace ID)
     * 用于链路追踪
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * 生成唯一ID (使用UUID)
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    /**
     * 生成唯一ID (带短横线)
     */
    public static String generateUUIDWithDash() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成用户会话ID
     */
    public static String generateSessionId() {
        return String.format("SESSION-%s-%s", System.currentTimeMillis(), generateUUID());
    }
}
