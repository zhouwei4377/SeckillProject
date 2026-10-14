package com.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 补偿记录：用于记录库存恢复/补偿任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compensation_record")
public class CompensationRecord {

    @TableId
    private Long id;

    private Long orderId;

    private Long productId;

    private Integer quantity;

    /**
     * 类型：1=restore-stock 2=other
     */
    private Integer type;

    /**
     * 状态：0=pending 1=success 2=failed
     */
    private Integer status;

    private Integer attempts;

    private LocalDateTime lastAttemptTime;

    private LocalDateTime createTime;
}
