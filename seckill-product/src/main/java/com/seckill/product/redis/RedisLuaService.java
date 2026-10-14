package com.seckill.product.redis;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Redis Lua 脚本执行服务
 */
@Component
public class RedisLuaService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<Long> decrScript;
    private DefaultRedisScript<Long> incrScript;

    @PostConstruct
    public void init() throws Exception {
        decrScript = new DefaultRedisScript<>();
        decrScript.setResultType(Long.class);
        Resource r = new ClassPathResource("scripts/seckill_decr.lua");
        try (InputStream in = r.getInputStream()) {
            byte[] bs = in.readAllBytes();
            String script = new String(bs, StandardCharsets.UTF_8);
            decrScript.setScriptText(script);
        }

        incrScript = new DefaultRedisScript<>();
        incrScript.setResultType(Long.class);
        Resource r2 = new ClassPathResource("scripts/seckill_revert.lua");
        try (InputStream in = r2.getInputStream()) {
            byte[] bs = in.readAllBytes();
            String script = new String(bs, StandardCharsets.UTF_8);
            incrScript.setScriptText(script);
        }
    }

    public Long decr(String stockKey, int qty) {
        return redisTemplate.execute(decrScript, Collections.singletonList(stockKey), String.valueOf(qty));
    }

    public Long incr(String stockKey, int qty) {
        return redisTemplate.execute(incrScript, Collections.singletonList(stockKey), String.valueOf(qty));
    }
}
