package com.seckill.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * IP工具类
 * 用于获取客户端真实IP地址
 */
public class IpUtil {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "::1";
    
    /**
     * 获取客户端真实IP地址
     * 支持代理环境下的IP获取
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        // 尝试从各种代理头部获取IP
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多个代理的情况，第一个IP为客户端真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            }
            return ip.trim();
        }
        
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isNotEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        
        // 从请求连接获取IP
        ip = request.getRemoteAddr();
        
        return ip;
    }
    
    /**
     * 检查是否为本地IP
     */
    public static boolean isLocalIp(String ip) {
        return LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip);
    }
    
    /**
     * 验证IP格式
     */
    public static boolean isValidIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")
            || ip.matches("^([a-f0-9:]+:+)+[a-f0-9]+$");
    }
    
    /**
     * 计算IP的哈希值（用于分布式限流）
     */
    public static int hashIp(String ip) {
        return ip.hashCode() % 256;
    }
}
