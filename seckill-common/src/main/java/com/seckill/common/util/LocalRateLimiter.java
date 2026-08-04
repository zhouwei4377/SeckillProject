package com.seckill.common.util;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地内存限流工具
 * 使用 Guava RateLimiter 实现令牌桶算法
 */
@Slf4j
public class LocalRateLimiter {
    
    private static final ConcurrentHashMap<String, RateLimiter> LIMITERS = new ConcurrentHashMap<>();
    
    /**
     * 获取或创建限流器
     */
    public static RateLimiter getOrCreate(String key, double permitsPerSecond) {
        return LIMITERS.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    }
    
    /**
     * 尝试获取许可 (非阻塞)
     */
    public static boolean tryAcquire(String key, double permitsPerSecond) {
        RateLimiter limiter = getOrCreate(key, permitsPerSecond);
        boolean acquired = limiter.tryAcquire();
        if (!acquired) {
            log.warn("限流触发: {}", key);
        }
        return acquired;
    }
    
    /**
     * 尝试获取许可 (非阻塞, 支持多个许可)
     */
    public static boolean tryAcquire(String key, double permitsPerSecond, int permits) {
        RateLimiter limiter = getOrCreate(key, permitsPerSecond);
        boolean acquired = limiter.tryAcquire(permits);
        if (!acquired) {
            log.warn("限流触发: {} (permits: {})", key, permits);
        }
        return acquired;
    }
    
    /**
     * 获取许可 (阻塞)
     */
    public static void acquire(String key, double permitsPerSecond) {
        RateLimiter limiter = getOrCreate(key, permitsPerSecond);
        limiter.acquire();
    }
    
    /**
     * 获取许可 (阻塞, 支持多个许可)
     */
    public static void acquire(String key, double permitsPerSecond, int permits) {
        RateLimiter limiter = getOrCreate(key, permitsPerSecond);
        limiter.acquire(permits);
    }
    
    /**
     * 删除限流器
     */
    public static void remove(String key) {
        LIMITERS.remove(key);
    }
    
    /**
     * 清空所有限流器
     */
    public static void clear() {
        LIMITERS.clear();
    }
}
