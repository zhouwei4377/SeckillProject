package com.seckill.common.model;

import com.alibaba.fastjson2.annotation.JSONField;
import com.seckill.common.constant.SeckillConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 统一响应模型
 * 规范所有接口的返回格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    /**
     * 响应状态码 (业务码)
     */
    private String code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    @JSONField(name = "timestamp")
    private long timestamp;
    
    /**
     * 请求追踪ID
     */
    @JSONField(name = "trace_id")
    private String traceId;
    
    // ==================== 成功响应 ====================
    
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "成功");
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .code(SeckillConstants.ERROR_CODE_SUCCESS)
            .message(message)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // ==================== 失败响应 ====================
    
    public static <T> ApiResponse<T> fail(String code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // ==================== 特定错误响应 ====================
    
    public static <T> ApiResponse<T> rateLimitExceeded() {
        return fail(SeckillConstants.ERROR_CODE_RATE_LIMIT, "请求过于频繁，请稍候再试");
    }
    
    public static <T> ApiResponse<T> stockInsufficient() {
        return fail(SeckillConstants.ERROR_CODE_STOCK_INSUFFICIENT, "库存不足，秒杀失败");
    }
    
    public static <T> ApiResponse<T> seckillNotStart() {
        return fail(SeckillConstants.ERROR_CODE_SECKILL_NOT_START, "秒杀活动未开始");
    }
    
    public static <T> ApiResponse<T> seckillEnded() {
        return fail(SeckillConstants.ERROR_CODE_SECKILL_ENDED, "秒杀活动已结束");
    }
    
    public static <T> ApiResponse<T> duplicateRequest() {
        return fail(SeckillConstants.ERROR_CODE_DUPLICATE_REQUEST, "请求已提交，请勿重复提交");
    }
    
    public static <T> ApiResponse<T> invalidParameter(String message) {
        return fail(SeckillConstants.ERROR_CODE_INVALID_PARAM, message);
    }
    
    public static <T> ApiResponse<T> systemError() {
        return fail(SeckillConstants.ERROR_CODE_SYSTEM_ERROR, "系统异常，请稍候再试");
    }
    
    // ==================== 检查方法 ====================
    
    public boolean isSuccess() {
        return SeckillConstants.ERROR_CODE_SUCCESS.equals(this.code);
    }
    
    public boolean isFail() {
        return !isSuccess();
    }
}
