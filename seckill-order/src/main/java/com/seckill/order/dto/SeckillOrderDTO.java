package com.seckill.order.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeckillOrderDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    @JSONField(name = "order_no")
    private String orderNo;
    
    @JSONField(name = "user_id")
    private Long userId;
    
    @JSONField(name = "product_id")
    private Long productId;
    
    private Integer quantity;
    
    @JSONField(name = "seckill_price")
    private BigDecimal seckillPrice;
    
    @JSONField(name = "total_price")
    private BigDecimal totalPrice;
    
    private Integer status;
    
    @JSONField(name = "payment_method")
    private Integer paymentMethod;
    
    private String phone;
    
    @JSONField(name = "receiver_name")
    private String receiverName;
    
    @JSONField(name = "delivery_address")
    private String deliveryAddress;
    
    @JSONField(name = "create_time")
    private LocalDateTime createTime;
    
    @JSONField(name = "payment_time")
    private LocalDateTime paymentTime;
}
