package com.seckill.product.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisLuaServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RedisLuaService redisLuaService;

    @Test
    void decr_shouldExecuteLuaScriptAndReturnResult() throws Exception {
        ReflectionTestUtils.setField(redisLuaService, "redisTemplate", redisTemplate);
        redisLuaService.init();

        when(redisTemplate.execute(any(), eq(Collections.singletonList("seckill:product:stock:1")), eq("1")))
                .thenReturn(9L);

        Long result = redisLuaService.decr("seckill:product:stock:1", 1);

        assertEquals(9L, result);
        verify(redisTemplate).execute(any(), eq(Collections.singletonList("seckill:product:stock:1")), eq("1"));
    }

    @Test
    void incr_shouldExecuteReverseLuaScriptAndReturnResult() throws Exception {
        ReflectionTestUtils.setField(redisLuaService, "redisTemplate", redisTemplate);
        redisLuaService.init();

        when(redisTemplate.execute(any(), eq(Collections.singletonList("seckill:product:stock:1")), eq("1")))
                .thenReturn(10L);

        Long result = redisLuaService.incr("seckill:product:stock:1", 1);

        assertEquals(10L, result);
        verify(redisTemplate).execute(any(), eq(Collections.singletonList("seckill:product:stock:1")), eq("1"));
    }
}
