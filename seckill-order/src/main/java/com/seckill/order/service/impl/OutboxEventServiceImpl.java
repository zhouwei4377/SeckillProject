package com.seckill.order.service.impl;

import com.seckill.common.util.JsonUtil;
import com.seckill.order.entity.OutboxEvent;
import com.seckill.order.mapper.OutboxEventMapper;
import com.seckill.order.service.OutboxEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OutboxEventServiceImpl implements OutboxEventService {

    @Resource
    private OutboxEventMapper outboxEventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(String aggregateType, String aggregateId, String eventType, Object payload) {
        if (aggregateType == null || aggregateId == null || eventType == null) {
            throw new IllegalArgumentException("outbox event metadata cannot be null");
        }

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(JsonUtil.toJsonString(payload));
        event.setStatus(0);
        event.setRetries(0);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        outboxEventMapper.insert(event);
    }

    @Override
    public List<OutboxEvent> fetchPendingEvents(int limit) {
        return outboxEventMapper.selectPendingByStatus(0, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSent(Long outboxId) {
        if (outboxId == null) {
            return;
        }
        OutboxEvent event = outboxEventMapper.selectById(outboxId);
        if (event == null) {
            return;
        }
        event.setStatus(1);
        event.setUpdatedAt(LocalDateTime.now());
        event.setLastError(null);
        outboxEventMapper.updateById(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long outboxId, String error) {
        if (outboxId == null) {
            return;
        }
        OutboxEvent event = outboxEventMapper.selectById(outboxId);
        if (event == null) {
            return;
        }
        event.setStatus(2);
        event.setRetries(event.getRetries() == null ? 1 : event.getRetries() + 1);
        event.setUpdatedAt(LocalDateTime.now());
        event.setLastError(error == null ? "unknown" : error.substring(0, Math.min(error.length(), 255)));
        outboxEventMapper.updateById(event);
    }
}
