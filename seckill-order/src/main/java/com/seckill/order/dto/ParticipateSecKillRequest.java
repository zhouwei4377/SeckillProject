package com.seckill.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 参加秒杀请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipateSecKillRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为nu null")
    private Long userId;
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为nu null")
    private Long productId;
    
    /**
     * 订单数量
     */
    @NotNull(message = "订单数量不能为nu null")
    @Min(value = 1, message = "订单数量最小为1")
    private Integer quantity;
    
    /**
     * 请求唯一ID (用于幂等性)
     */
    private String requestId;
}
