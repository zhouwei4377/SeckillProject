package com.seckill.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为null")
    private Long orderId;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为null")
    private Long userId;
    
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为null")
    @Min(value = 0, message = "支付金额不能为负数")
    private BigDecimal amount;
    
    /**
     * 支付方式 (1:在线支付, 2:余额支付)
     */
    @NotNull(message = "支付方式不能为null")
    private Integer paymentMethod;
    
    /**
     * 支付渠道 (1:支付宝, 2:微信)
     */
    private Integer paymentChannel;
    
    /**
     * 支付密码/验证码
     */
    private String paymentPassword;
}
