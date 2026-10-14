package com.seckill.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.order.entity.CompensationRecord;
import com.seckill.order.mapper.CompensationRecordMapper;
import com.seckill.order.service.CompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 简单的补偿服务：定时扫描未成功的补偿记录并重试
 */
@Slf4j
@Service
public class CompensationServiceImpl implements CompensationService {

    @Resource
    private CompensationRecordMapper compensationRecordMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${seckill.compensation.max-attempts:5}")
    private int maxAttempts;

    @Value("${seckill.compensation.retry-interval-ms:5000}")
    private long retryIntervalMs;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRestoreStockCompensation(Long orderId, Long productId, Integer quantity) {
        try {
            CompensationRecord rec = CompensationRecord.builder()
                    .orderId(orderId)
                    .productId(productId)
                    .quantity(quantity)
                    .type(1)
                    .status(0)
                    .attempts(0)
                    .createTime(LocalDateTime.now())
                    .build();
            compensationRecordMapper.insert(rec);
            log.info("已写入补偿记录 orderId={}, productId={}, qty={}", orderId, productId, quantity);
        } catch (Exception e) {
            log.error("提交补偿记录失败", e);
        }
    }

    // 每隔配置的间隔扫描一次未成功的补偿记录并尝试恢复库存
    @Scheduled(fixedDelayString = "${seckill.compensation.scan-interval-ms:60000}")
    public void scanAndRetry() {
        log.debug("补偿扫描开始");
        LambdaQueryWrapper<CompensationRecord> wrapper = new LambdaQueryWrapper<CompensationRecord>()
                .eq(CompensationRecord::getStatus, 0)
                .lt(CompensationRecord::getAttempts, maxAttempts);
        List<CompensationRecord> list = compensationRecordMapper.selectList(wrapper);
        for (CompensationRecord rec : list) {
            try {
                // 控制并发重试节奏
                String lockKey = "seckill:compensation:lock:" + rec.getId();
                Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(locked)) {
                    continue;
                }

                boolean success = false;
                if (rec.getType() == 1) {
                    // 调用商品服务恢复库存
                    try {
                        String url = "http://seckill-product/internal/product/{id}/restoreStock";
                        Map<String, Object> body = new HashMap<>();
                        body.put("quantity", rec.getQuantity());
                        Boolean result = restTemplate.postForObject(url, body, Boolean.class, rec.getProductId());
                        success = Boolean.TRUE.equals(result);
                    } catch (Exception ex) {
                        log.warn("补偿调用商品服务失败 recId={}, orderId={}", rec.getId(), rec.getOrderId(), ex);
                    }
                }

                rec.setAttempts(rec.getAttempts() + 1);
                rec.setLastAttemptTime(LocalDateTime.now());
                if (success) {
                    rec.setStatus(1);
                    compensationRecordMapper.updateById(rec);
                    log.info("补偿成功 recId={}, orderId={}", rec.getId(), rec.getOrderId());
                } else {
                    if (rec.getAttempts() >= maxAttempts) {
                        rec.setStatus(2);
                        compensationRecordMapper.updateById(rec);
                        log.warn("补偿结束，达到最大重试次数 recId={}, orderId={}", rec.getId(), rec.getOrderId());
                    } else {
                        compensationRecordMapper.updateById(rec);
                        log.info("补偿将重试 recId={}, nextAttempt={} ", rec.getId(), rec.getAttempts()+1);
                    }
                }

                // 释放锁（key 会自动过期）
                redisTemplate.delete(lockKey);

                // sleep between retries to avoid thundering herd
                Thread.sleep(Math.max(100L, retryIntervalMs));

            } catch (Exception e) {
                log.error("补偿执行异常 recId={}", rec.getId(), e);
            }
        }
        log.debug("补偿扫描结束");
    }
}
