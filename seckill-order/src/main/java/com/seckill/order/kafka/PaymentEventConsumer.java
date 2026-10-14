package com.seckill.order.kafka;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.OutboxEventMapper;
import com.seckill.order.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class PaymentEventConsumer {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private OutboxEventMapper outboxEventMapper;

    @KafkaListener(topics = SeckillConstants.TOPIC_PAYMENT_SUCCESS, groupId = SeckillConstants.GROUP_PAYMENT_SERVICE)
    @Transactional(rollbackFor = Exception.class)
    public void onPaymentSuccess(String message) {
        log.info("接收到支付成功事件: {}", message);
        try {
            Map<String, Object> payload = JSON.parseObject(message, Map.class);
            if (payload == null) {
                log.warn("无效的支付消息: {}", message);
                return;
            }

            Object orderIdObj = payload.get("orderId");
            Long orderId = null;
            if (orderIdObj instanceof Number) {
                orderId = ((Number) orderIdObj).longValue();
            } else if (orderIdObj instanceof String) {
                orderId = Long.parseLong((String) orderIdObj);
            }

            if (orderId == null) {
                log.warn("支付消息中未包含orderId，消息: {}", message);
                return;
            }

            String dedupKey = "payment:processed:" + orderId;
            if (isProcessed(dedupKey)) {
                log.info("支付事件已处理，跳过 orderId={}", orderId);
                return;
            }

            SeckillOrder dbOrder = seckillOrderMapper.selectById(orderId);
            if (dbOrder == null) {
                log.warn("订单不存在，orderId={}", orderId);
                return;
            }

            if (dbOrder.getStatus() == SeckillConstants.ORDER_STATUS_PAID
                    || dbOrder.getStatus() == SeckillConstants.ORDER_STATUS_SHIPPED
                    || dbOrder.getStatus() == SeckillConstants.ORDER_STATUS_RECEIVED) {
                markProcessed(dedupKey);
                return;
            }

            dbOrder.setStatus(SeckillConstants.ORDER_STATUS_PAID);
            dbOrder.setPaymentTime(LocalDateTime.now());
            seckillOrderMapper.updateById(dbOrder);
            markProcessed(dedupKey);

            log.info("订单已标记为已支付，orderId={}", orderId);
        } catch (Exception e) {
            log.error("处理支付成功事件异常", e);
            throw e;
        }
    }

    private boolean isProcessed(String dedupKey) {
        return outboxEventMapper.selectList(null).stream()
                .anyMatch(event -> event.getEventType() != null && event.getEventType().contains(dedupKey));
    }

    private void markProcessed(String dedupKey) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("kafka-dedup");
        event.setAggregateId(dedupKey);
        event.setEventType("processed");
        event.setPayload("{}");
        event.setStatus(1);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        outboxEventMapper.insert(event);
    }
}
