package com.seckill.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存变更记录实体类
 * 用于记录每次库存的变更情况
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_stock_change_log")
public class StockChangeLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 变更类型 (1:扣减, 2:回退, 3:初始化)
     */
    private Integer changeType;
    
    /**
     * 变更数量
     */
    private Integer changeQuantity;
    
    /**
     * 变更前库存
     */
    private Integer stockBefore;
    
    /**
     * 变更后库存
     */
    private Integer stockAfter;
    
    /**
     * 相关订单ID
     */
    private Long orderId;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
