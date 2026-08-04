package com.seckill.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滑动窗口限流实现
 * 用于精确的频率统计
 */
public class SlidingWindowRateLimiter {
    
    private static final class Window {
        private final long startTime;         // 窗口开始时间
        private final AtomicInteger count;    // 窗口内计数
        private final long windowSize;        // 窗口大小（毫秒）
        
        Window(long windowSize) {
            this.startTime = System.currentTimeMillis();
            this.count = new AtomicInteger(0);
            this.windowSize = windowSize;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - startTime > windowSize;
        }
    }
    
    private static final ConcurrentHashMap<String, Window> WINDOWS = new ConcurrentHashMap<>();
    
    /**
     * 检查是否超过限制
     * @param key 限流key
     * @param limit 时间窗口内的最大请求数
     * @param windowSize 时间窗口大小（毫秒）
     * @return 是否超过限制
     */
    public static boolean isExceeded(String key, int limit, long windowSize) {
        if (StringUtils.isEmpty(key)) {
            return true;
        }
        
        Window window = WINDOWS.get(key);
        
        if (window == null || window.isExpired()) {
            // 创建新窗口
            Window newWindow = new Window(windowSize);
            newWindow.count.incrementAndGet();
            WINDOWS.put(key, newWindow);
            return false;
        }
        
        // 增加计数
        int currentCount = window.count.incrementAndGet();
        return currentCount > limit;
    }
    
    /**
     * 获取当前窗口内的请求数
     */
    public static int getCurrentCount(String key) {
        Window window = WINDOWS.get(key);
        if (window == null || window.isExpired()) {
            return 0;
        }
        return window.count.get();
    }
    
    /**
     * 清空过期的窗口
     */
    public static void cleanExpiredWindows() {
        WINDOWS.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * 删除指定key的窗口
     */
    public static void remove(String key) {
        WINDOWS.remove(key);
    }
    
    /**
     * 清空所有窗口
     */
    public static void clear() {
        WINDOWS.clear();
    }
}
