package com.seckill.order.kafka;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.OutboxEvent;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.OutboxEventMapper;
import com.seckill.order.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class OrderEventConsumer {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private OutboxEventMapper outboxEventMapper;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = SeckillConstants.TOPIC_ORDER_CREATE, groupId = SeckillConstants.GROUP_ORDER_SERVICE)
    @Transactional(rollbackFor = Exception.class)
    public void onOrderCreated(String message) {
        log.info("接收到订单创建事件: {}", message);
        try {
            SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
            if (order == null || order.getId() == null) {
                log.warn("无效的订单消息: {}", message);
                return;
            }

            Long orderId = order.getId();
            String dedupKey = "order:create:processed:" + orderId;
            if (isProcessed(dedupKey)) {
                log.info("订单创建事件已处理，跳过 orderId={}", orderId);
                return;
            }

            SeckillOrder dbOrder = seckillOrderMapper.selectById(orderId);
            if (dbOrder == null) {
                log.warn("订单不存在，orderId={}", orderId);
                return;
            }

            if (dbOrder.getStatus() == null || dbOrder.getStatus() != SeckillConstants.ORDER_STATUS_PENDING) {
                log.info("订单状态不允许处理，orderId={}, status={}", orderId, dbOrder.getStatus());
                markProcessed(dedupKey);
                return;
            }

            markProcessed(dedupKey);
            log.info("订单创建事件已处理 orderId={}", orderId);
        } catch (Exception e) {
            log.error("处理订单创建事件异常", e);
            throw e;
        }
    }

    @KafkaListener(topics = SeckillConstants.TOPIC_ORDER_CANCEL, groupId = SeckillConstants.GROUP_ORDER_SERVICE)
    @Transactional(rollbackFor = Exception.class)
    public void onOrderCancelled(String message) {
        log.info("接收到订单取消事件: {}", message);
        try {
            SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
            if (order == null || order.getId() == null) {
                log.warn("无效的订单取消消息: {}", message);
                return;
            }

            Long orderId = order.getId();
            String dedupKey = "order:cancel:processed:" + orderId;
            if (isProcessed(dedupKey)) {
                log.info("订单取消事件已处理，跳过 orderId={}", orderId);
                return;
            }

            SeckillOrder dbOrder = seckillOrderMapper.selectById(orderId);
            if (dbOrder != null && dbOrder.getStatus() != SeckillConstants.ORDER_STATUS_CANCELLED) {
                dbOrder.setStatus(SeckillConstants.ORDER_STATUS_CANCELLED);
                dbOrder.setCancelTime(LocalDateTime.now());
                seckillOrderMapper.updateById(dbOrder);
            }

            markProcessed(dedupKey);
            log.info("订单取消事件处理完成 orderId={}", orderId);
        } catch (Exception e) {
            log.error("处理订单取消事件异常", e);
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
