package com.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("compensation_record")
public class CompensationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private Integer quantity;

    private Integer type;

    private Integer status;

    private Integer attempts;

    private Integer maxAttempts;

    private LocalDateTime nextRetryTime;

    private String errorMessage;

    private String payload;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
