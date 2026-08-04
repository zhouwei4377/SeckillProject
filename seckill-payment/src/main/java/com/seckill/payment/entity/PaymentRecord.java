package com.seckill.payment.entity;

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
 * 支付记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_payment_record")
public class PaymentRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 支付记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 支付单号
     */
    private String paymentNo;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式 (1:在线支付, 2:余额支付, 3:积分支付)
     */
    private Integer paymentMethod;
    
    /**
     * 支付状态 (1:待支付, 2:支付成功, 3:支付失败, 4:已退款)
     */
    private Integer status;
    
    /**
     * 第三方支付单号
     */
    private String thirdPartyPaymentNo;
    
    /**
     * 支付渠道 (1:支付宝, 2:微信, 3:银联)
     */
    private Integer paymentChannel;
    
    /**
     * 支付备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 支付完成时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 版本号 (乐观锁)
     */
    private Integer version;
    
    /**
     * 是否删除 (0:否, 1:是)
     */
    private Integer deleted;
}
