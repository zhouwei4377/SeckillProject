package com.seckill.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_seckill_product")
public class SeckillProduct implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品名称
     */
    private String name;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 秒杀价
     */
    private BigDecimal seckillPrice;
    
    /**
     * 初始库存
     */
    private Integer initialStock;
    
    /**
     * 当前库存
     */
    private Integer currentStock;
    
    /**
     * 已售数量
     */
    private Integer soldQuantity;
    
    /**
     * 秒杀开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 秒杀状态 (1:未开始, 2:进行中, 3:已结束, 4:已售罄)
     */
    private Integer status;
    
    /**
     * 商品图片URL
     */
    private String imageUrl;
    
    /**
     * 库存版本号 (用于CAS乐观锁更新)
     */
    private Integer stockVersion;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除 (0:否, 1:是)
     */
    private Integer deleted;
}
