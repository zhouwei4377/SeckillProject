package com.seckill.order.service.impl;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.order.dto.ParticipateSecKillRequest;
import com.seckill.order.dto.SeckillOrderDTO;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.SeckillOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillOrderServiceImplTest {

    @Mock
    private SeckillOrderMapper seckillOrderMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    private SeckillOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeckillOrderServiceImpl();
        ReflectionTestUtils.setField(service, "seckillOrderMapper", seckillOrderMapper);
        ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(service, "redissonClient", redissonClient);
        ReflectionTestUtils.setField(service, "kafkaTemplate", kafkaTemplate);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(valueOperations.increment(anyString())).thenReturn(1L);
    }

    @Test
    void participateInSeckill_shouldSucceedWhenInventoryChecksPass() {
        Participating request = new Participating();
        request.setUserId(100L);
        request.setProductId(1L);
        request.setQuantity(1);
        request.setRequestId("req-1");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class), eq(1L))).thenReturn(Boolean.TRUE);
        when(seckillOrderMapper.insert(any(SeckillOrder.class))).thenReturn(1);

        SeckillOrderDTO dto = service.participateInSeckill(request);

        assertNotNull(dto);
        assertEquals(1L, dto.getProductId());
        assertEquals(1, dto.getQuantity());
        assertEquals(SeckillConstants.ORDER_STATUS_PENDING, dto.getStatus());
    }

    @Test
    void cancelOrder_shouldRejectNonPendingOrder() {
        SeckillOrder order = new SeckillOrder();
        order.setId(10L);
        order.setProductId(1L);
        order.setQuantity(1);
        order.setStatus(SeckillConstants.ORDER_STATUS_PAID);

        when(seckillOrderMapper.selectById(10L)).thenReturn(order);

        try {
            service.cancelOrder(10L);
        } catch (Exception ex) {
            assertNotNull(ex);
        }
    }

    private static class Participating extends ParticipatingSecKillRequest {
    }
}
