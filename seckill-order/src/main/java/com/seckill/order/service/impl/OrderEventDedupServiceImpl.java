package com.seckill.order.service.impl;

import com.seckill.order.mapper.OrderEventDedupMapper;
import com.seckill.order.service.OrderEventDedupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderEventDedupServiceImpl implements OrderEventDedupService {

    @Resource
    private OrderEventDedupMapper orderEventDedupMapper;

    @Override
    public boolean tryAcquire(String eventType, String aggregateId) {
        if (eventType == null || aggregateId == null) {
            return false;
        }
        return orderEventDedupMapper.insertIfAbsent(eventType, aggregateId) > 0;
    }
}
