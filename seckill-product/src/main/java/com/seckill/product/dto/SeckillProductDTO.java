package com.seckill.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品详情DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeckillProductDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer initialStock;
    private Integer currentStock;
    private Integer soldQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String imageUrl;
    
    // 计算字段
    private Double discountRate;  // 折扣率
    private Integer remainingTime; // 剩余秒数
    private Boolean isSoldOut;     // 是否售罄
}
