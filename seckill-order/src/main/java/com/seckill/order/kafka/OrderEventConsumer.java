package com.seckill.order.kafka;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 订单事件消费者（幂等处理）
 */
@Slf4j
@Component
public class OrderEventConsumer {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private RestTemplate restTemplate;

    @KafkaListener(topics = SeckillConstants.TOPIC_ORDER_CREATE, groupId = SeckillConstants.GROUP_ORDER_SERVICE)
    public void onOrderCreated(String message) {
        log.info("接收到订单创建事件: {}", message);
        try {
            SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
            if (order == null || order.getId() == null) {
                log.warn("无效的订单消息: {}", message);
                return;
            }

            String dedupKey = "seckill:order:processed:" + order.getId();
            Boolean added = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(added)) {
                log.info("订单事件已处理过，跳过 orderId={}", order.getId());
                return;
            }

            // 幂等检查通过，执行业务（此处为示例：确认订单在 DB 中是已创建状态）
            SeckillOrder dbOrder = seckillOrderMapper.selectById(order.getId());
            if (dbOrder == null) {
                log.warn("订单在数据库不存在，orderId={}", order.getId());
                return;
            }

            // 示例逻辑：如果订单仍然是待支付状态，通知支付系统或埋点（这里只记录日志）
            if (dbOrder.getStatus() == SeckillConstants.ORDER_STATUS_PENDING) {
                log.info("订单处于待支付状态，继续后续处理 orderId={}", order.getId());
                // TODO: 调用支付系统、推送消息等
            }

            log.info("订单创建事件处理完成 orderId={}", order.getId());

        } catch (Exception e) {
            log.error("处理订单创建事件异常", e);
            throw e; // 让 Kafka 托管重试/死信策略
        }
    }

    @KafkaListener(topics = SeckillConstants.TOPIC_ORDER_CANCEL, groupId = SeckillConstants.GROUP_ORDER_SERVICE)
    public void onOrderCancelled(String message) {
        log.info("接收到订单取消事件: {}", message);
        try {
            SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
            if (order == null || order.getId() == null) {
                log.warn("无效的订单取消消息: {}", message);
                return;
            }

            String dedupKey = "seckill:order:cancel:processed:" + order.getId();
            Boolean added = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(added)) {
                log.info("订单取消事件已处理过，跳过 orderId={}", order.getId());
                return;
            }

            // 调用商品服务恢复库存
            try {
                String url = "http://seckill-product/internal/product/{id}/restoreStock";
                Map<String, Object> body = new HashMap<>();
                body.put("quantity", order.getQuantity());
                Boolean result = restTemplate.postForObject(url, body, Boolean.class, order.getProductId());
                if (Boolean.TRUE.equals(result)) {
                    log.info("恢复库存成功，orderId={}, productId={}, qty={}", order.getId(), order.getProductId(), order.getQuantity());
                } else {
                    log.warn("恢复库存失败（商品服务返回false），orderId={}", order.getId());
                }
            } catch (Exception ex) {
                log.error("调用商品服务恢复库存异常，orderId={}", order.getId(), ex);
                throw ex; // 触发 kafka 重试/死信
            }

            // 更新订单状态为已取消（幂等地更新）
            try {
                SeckillOrder dbOrder = seckillOrderMapper.selectById(order.getId());
                if (dbOrder != null && dbOrder.getStatus() != SeckillConstants.ORDER_STATUS_CANCELLED) {
                    dbOrder.setStatus(SeckillConstants.ORDER_STATUS_CANCELLED);
                    seckillOrderMapper.updateById(dbOrder);
                    log.info("订单状态更新为已取消 orderId={}", order.getId());
                } else {
                    log.info("订单已是取消状态或不存在，orderId={}", order.getId());
                }
            } catch (Exception ex) {
                log.error("更新订单取消状态失败 orderId={}", order.getId(), ex);
                throw ex;
            }

            log.info("订单取消事件处理完成 orderId={}", order.getId());

        } catch (Exception e) {
            log.error("处理订单取消事件异常", e);
            throw e;
        }
    }
}
