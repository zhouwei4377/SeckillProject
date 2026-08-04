package com.seckill.common.exception;

import com.seckill.common.constant.SeckillConstants;
import lombok.Getter;

/**
 * 秒杀系统自定义异常
 * 提供统一的异常处理
 */
@Getter
public class SeckillException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    private String errorMessage;
    private int httpStatus;
    private Object data;
    
    public SeckillException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, SeckillConstants.HTTP_SERVER_ERROR);
    }
    
    public SeckillException(String errorCode, String errorMessage, int httpStatus) {
        this(errorCode, errorMessage, httpStatus, null);
    }
    
    public SeckillException(String errorCode, String errorMessage, int httpStatus, Object data) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
        this.data = data;
    }
    
    // 常见异常工厂方法
    public static SeckillException rateLimitExceeded() {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_RATE_LIMIT,
            "请求过于频繁，请稍候再试",
            SeckillConstants.HTTP_RATE_LIMIT
        );
    }
    
    public static SeckillException stockInsufficient() {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_STOCK_INSUFFICIENT,
            "库存不足，秒杀失败",
            SeckillConstants.HTTP_CONFLICT
        );
    }
    
    public static SeckillException seckillNotStart() {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_SECKILL_NOT_START,
            "秒杀活动未开始",
            SeckillConstants.HTTP_BAD_REQUEST
        );
    }
    
    public static SeckillException seckillEnded() {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_SECKILL_ENDED,
            "秒杀活动已结束",
            SeckillConstants.HTTP_BAD_REQUEST
        );
    }
    
    public static SeckillException duplicateRequest() {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_DUPLICATE_REQUEST,
            "请求已提交，请勿重复提交",
            SeckillConstants.HTTP_CONFLICT
        );
    }
    
    public static SeckillException invalidParameter(String message) {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_INVALID_PARAM,
            message,
            SeckillConstants.HTTP_BAD_REQUEST
        );
    }
    
    public static SeckillException systemError(String message) {
        return new SeckillException(
            SeckillConstants.ERROR_CODE_SYSTEM_ERROR,
            message,
            SeckillConstants.HTTP_SERVER_ERROR
        );
    }
}
