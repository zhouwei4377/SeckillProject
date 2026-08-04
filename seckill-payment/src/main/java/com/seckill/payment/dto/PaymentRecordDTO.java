package com.seckill.payment.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecordDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    @JSONField(name = "payment_no")
    private String paymentNo;
    
    @JSONField(name = "order_id")
    private Long orderId;
    
    @JSONField(name = "order_no")
    private String orderNo;
    
    @JSONField(name = "user_id")
    private Long userId;
    
    private BigDecimal amount;
    
    @JSONField(name = "payment_method")
    private Integer paymentMethod;
    
    private Integer status;
    
    @JSONField(name = "payment_channel")
    private Integer paymentChannel;
    
    private String remark;
    
    @JSONField(name = "create_time")
    private LocalDateTime createTime;
    
    @JSONField(name = "payment_time")
    private LocalDateTime paymentTime;
}
