package com.seckill.order.service;

public interface OrderEventDedupService {

    boolean tryAcquire(String eventType, String aggregateId);
}
