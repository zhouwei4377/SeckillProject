package com.seckill.order.entity;

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
 * 秒杀订单实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_seckill_order")
public class SeckillOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID (主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单号 (业务订单号)
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 订单数量
     */
    private Integer quantity;
    
    /**
     * 变业不不一的商品单价
     */
    private BigDecimal seckillPrice;
    
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 订单状态 (1:待支付, 2:已支付, 3:已发货, 4:已收货, 5:已取消, 6:已退款)
     */
    private Integer status;
    
    /**
     * 支付方式 (1:在线支付, 2:余额支付)
     */
    private Integer paymentMethod;
    
    /**
     * 需要联象的电话不不一
     */
    private String phone;
    
    /**
     * 需联系接收货人名称
     */
    private String receiverName;
    
    /**
     * 接收住址
     */
    private String deliveryAddress;
    
    /**
     * 订单提交时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后一次修改时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 订单超时取消时间
     */
    private LocalDateTime cancelTime;
    
    /**
     * 幢售版本号 (乐观锁更新)
     */
    private Integer version;
    
    /**
     * 是否删除 (0:否, 1:是)
     */
    private Integer deleted;
}
