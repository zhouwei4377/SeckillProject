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
@TableName("order_event_dedup")
public class OrderEventDedup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventType;

    private String aggregateId;

    private LocalDateTime createdAt;
}
