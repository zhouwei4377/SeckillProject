package com.seckill.order.service;

import com.seckill.order.entity.OutboxEvent;

import java.util.List;

public interface OutboxEventService {

    void publish(String aggregateType, String aggregateId, String eventType, Object payload);

    List<OutboxEvent> fetchPendingEvents(int limit);

    void markSent(Long outboxId);

    void markFailed(Long outboxId, String error);
}
