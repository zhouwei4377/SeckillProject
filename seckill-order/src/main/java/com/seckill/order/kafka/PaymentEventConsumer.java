package com.seckill.order.kafka;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 支付成功事件消费者：将订单标记为已支付，做幂等保护
 */
@Slf4j
@Component
public class PaymentEventConsumer {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @KafkaListener(topics = SeckillConstants.TOPIC_PAYMENT_SUCCESS, groupId = SeckillConstants.GROUP_PAYMENT_SERVICE)
    public void onPaymentSuccess(String message) {
        log.info("接收到支付成功事件: {}", message);
        try {
            // 支付消息格式不确定，尝试解析为 Map
            Map<String, Object> payload = JSON.parseObject(message, Map.class);
            if (payload == null) {
                log.warn("无效的支付消息: {}", message);
                return;
            }

            // 优先尝试获取 orderId 字段
            Object orderIdObj = payload.get("orderId");
            Long orderId = null;
            if (orderIdObj instanceof Number) {
                orderId = ((Number) orderIdObj).longValue();
            } else if (orderIdObj instanceof String) {
                try {
                    orderId = Long.parseLong((String) orderIdObj);
                } catch (NumberFormatException ignored) {
                }
            }

            if (orderId == null) {
                log.warn("支付消息中未包含orderId，消息: {}", message);
                return;
            }

            String dedupKey = "seckill:payment:processed:" + orderId;
            Boolean added = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(added)) {
                log.info("支付事件已处理过，跳过 orderId={}", orderId);
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
                log.info("订单已支付或已后续处理，无需重复设置，orderId={}", orderId);
                return;
            }

            // 更新订单为已支付
            dbOrder.setStatus(SeckillConstants.ORDER_STATUS_PAID);
            dbOrder.setPaymentTime(LocalDateTime.now());
            seckillOrderMapper.updateById(dbOrder);

            log.info("订单已标记为已支付，orderId={}", orderId);

            // TODO: 可以发布后续事件（发货、统计等）

        } catch (Exception e) {
            log.error("处理支付成功事件异常", e);
            throw e; // 依赖 Kafka 重试/死信策略
        }
    }
}
