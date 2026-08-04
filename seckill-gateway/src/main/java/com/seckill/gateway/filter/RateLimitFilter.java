package com.seckill.gateway.filter;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.util.IpUtil;
import com.seckill.common.util.LocalRateLimiter;
import com.seckill.common.util.SlidingWindowRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Zuul 限流过滤器
 * 在请求进入网关时进行限流
 */
@Slf4j
@Component
public class RateLimitFilter extends ZuulFilter {
    
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;  // 前置过滤器
    }
    
    @Override
    public int filterOrder() {
        return FilterConstants.DEBUG_FILTER_ORDER - 1;  // 高优先级
    }
    
    @Override
    public boolean shouldFilter() {
        return true;
    }
    
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        // 获取客户端IP
        String clientIp = IpUtil.getClientIp(request);
        
        // 检查IP黑名单
        if (isIpBlacklisted(clientIp)) {
            log.warn("���名单IP访问被拒绝: {}", clientIp);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(429);
            ctx.setResponseBody("请求被拒绝");
            return null;
        }
        
        // IP级别的限流
        if (isIpRateLimitExceeded(clientIp)) {
            log.warn("IP限流触发: {}", clientIp);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(429);
            ctx.setResponseBody("请求过于频繁，请稍候再试");
            return null;
        }
        
        // 用户级别的限流（如果有用户ID）
        String userId = request.getParameter("userId");
        if (userId != null && isUserRateLimitExceeded(userId)) {
            log.warn("用户限流触发: {}", userId);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(429);
            ctx.setResponseBody("您的请求过于频繁，请稍候再试");
            return null;
        }
        
        // 恶意流量检测
        if (detectMaliciousTraffic(clientIp)) {
            log.error("检测到恶意流量: {}", clientIp);
            addIpToBlacklist(clientIp);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            ctx.setResponseBody("访问被拒绝");
            return null;
        }
        
        return null;
    }
    
    /**
     * 检查IP是否在黑名单中
     */
    private boolean isIpBlacklisted(String ip) {
        // TODO: 从Redis获取黑名单
        return false;
    }
    
    /**
     * 检查IP是否超过限流阈值
     */
    private boolean isIpRateLimitExceeded(String ip) {
        String key = SeckillConstants.IP_RATE_LIMIT_KEY + ip;
        return SlidingWindowRateLimiter.isExceeded(
            key, 
            SeckillConstants.IP_RATE_LIMIT_PER_MINUTE,
            SeckillConstants.RATE_LIMIT_WINDOW * 1000
        );
    }
    
    /**
     * 检查用户是否超过限流阈值
     */
    private boolean isUserRateLimitExceeded(String userId) {
        String key = SeckillConstants.USER_RATE_LIMIT_KEY + userId;
        return SlidingWindowRateLimiter.isExceeded(
            key,
            SeckillConstants.USER_RATE_LIMIT_PER_MINUTE,
            SeckillConstants.RATE_LIMIT_WINDOW * 1000
        );
    }
    
    /**
     * 检测恶意流量
     * 检查在短时间内异常请求的频率
     */
    private boolean detectMaliciousTraffic(String ip) {
        String key = SeckillConstants.IP_RATE_LIMIT_KEY + ip + ":malicious";
        int currentCount = SlidingWindowRateLimiter.getCurrentCount(key);
        
        // 在30秒内超过阈值则判定为恶意流量
        return SlidingWindowRateLimiter.isExceeded(
            key,
            SeckillConstants.MALICIOUS_REQUEST_THRESHOLD,
            30 * 1000
        );
    }
    
    /**
     * 将IP添加到黑名单
     */
    private void addIpToBlacklist(String ip) {
        // TODO: 将IP添加到Redis黑名单，有效期为1小时
    }
}
