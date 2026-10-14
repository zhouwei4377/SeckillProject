package com.seckill.order.config;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.entity.OutboxEvent;
import com.seckill.order.service.OutboxEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class OutboxDispatcher {

    @Resource
    private OutboxEventService outboxEventService;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${seckill.outbox.fixed-delay:5000}")
    public void dispatchPendingEvents() {
        List<OutboxEvent> events = outboxEventService.fetchPendingEvents(50);
        if (events == null || events.isEmpty()) {
            return;
        }

        for (OutboxEvent event : events) {
            String topic = resolveTopic(event.getEventType());
            if (topic == null) {
                outboxEventService.markFailed(event.getId(), "unsupported event type: " + event.getEventType());
                continue;
            }

            try {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();
                outboxEventService.markSent(event.getId());
                log.info("outbox event sent: id={}, eventType={}, aggregateId={}, topic={}",
                        event.getId(), event.getEventType(), event.getAggregateId(), topic);
            } catch (Exception ex) {
                log.error("outbox event send failed: id={}, eventType={}, aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId(), ex);
                outboxEventService.markFailed(event.getId(), ex.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        if (SeckillConstants.TOPIC_ORDER_CREATE.equals(eventType)) {
            return SeckillConstants.TOPIC_ORDER_CREATE;
        }
        if (SeckillConstants.TOPIC_ORDER_CANCEL.equals(eventType)) {
            return SeckillConstants.TOPIC_ORDER_CANCEL;
        }
        if (SeckillConstants.TOPIC_PAYMENT_SUCCESS.equals(eventType)) {
            return SeckillConstants.TOPIC_PAYMENT_SUCCESS;
        }
        return null;
    }
}
