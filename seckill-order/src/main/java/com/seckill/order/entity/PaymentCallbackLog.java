package com.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("payment_callback_log")
public class PaymentCallbackLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String transactionId;

    private String status;

    private String payload;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
