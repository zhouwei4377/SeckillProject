package com.seckill.order.kafka;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.SeckillOrderMapper;
import com.seckill.order.service.OrderEventDedupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderEventConsumer {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private OrderEventDedupService orderEventDedupService;

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

            String dedupKey = "order:create:" + order.getId();
            if (!orderEventDedupService.tryAcquire(SeckillConstants.TOPIC_ORDER_CREATE, dedupKey)) {
                log.info("订单创建事件已处理，跳过 orderId={}", order.getId());
                return;
            }

            SeckillOrder dbOrder = seckillOrderMapper.selectById(order.getId());
            if (dbOrder == null) {
                log.warn("订单不存在，orderId={}", order.getId());
                return;
            }

            if (dbOrder.getStatus() == null || dbOrder.getStatus() != SeckillConstants.ORDER_STATUS_PENDING) {
                log.info("订单状态不允许处理，orderId={}, status={}", order.getId(), dbOrder.getStatus());
                return;
            }

            log.info("订单创建事件处理完成 orderId={}", order.getId());
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

            String dedupKey = "order:cancel:" + order.getId();
            if (!orderEventDedupService.tryAcquire(SeckillConstants.TOPIC_ORDER_CANCEL, dedupKey)) {
                log.info("订单取消事件已处理，跳过 orderId={}", order.getId());
                return;
            }

            SeckillOrder dbOrder = seckillOrderMapper.selectById(order.getId());
            if (dbOrder != null && dbOrder.getStatus() != SeckillConstants.ORDER_STATUS_CANCELLED) {
                dbOrder.setStatus(SeckillConstants.ORDER_STATUS_CANCELLED);
                dbOrder.setCancelTime(LocalDateTime.now());
                seckillOrderMapper.updateById(dbOrder);
            }

            log.info("订单取消事件处理完成 orderId={}", order.getId());
        } catch (Exception e) {
            log.error("处理订单取消事件异常", e);
            throw e;
        }
    }
}
