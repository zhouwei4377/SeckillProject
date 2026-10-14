package com.seckill.order.service.impl;

import com.seckill.order.entity.CompensationRecord;
import com.seckill.order.mapper.CompensationRecordMapper;
import com.seckill.order.service.CompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CompensationServiceImpl implements CompensationService {

    @Resource
    private CompensationRecordMapper compensationRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCompensation(Long orderId, Long productId, Integer quantity, String reason) {
        CompensationRecord record = new CompensationRecord();
        record.setOrderId(orderId);
        record.setProductId(productId);
        record.setQuantity(quantity == null ? 0 : quantity);
        record.setType(1);
        record.setStatus(1);
        record.setAttempts(0);
        record.setMaxAttempts(5);
        record.setNextRetryTime(LocalDateTime.now());
        record.setErrorMessage(reason);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        compensationRecordMapper.insert(record);
    }

    @Override
    public void retryPendingCompensations() {
        List<CompensationRecord> records = compensationRecordMapper.selectPendingCompensations(1, 5);
        for (CompensationRecord record : records) {
            try {
                if (record.getAttempts() >= record.getMaxAttempts()) {
                    record.setStatus(3);
                    record.setUpdateTime(LocalDateTime.now());
                    compensationRecordMapper.updateById(record);
                    continue;
                }
                record.setAttempts(record.getAttempts() + 1);
                record.setStatus(2);
                record.setUpdateTime(LocalDateTime.now());
                compensationRecordMapper.updateById(record);
                log.info("补偿任务执行成功: orderId={}, productId={}", record.getOrderId(), record.getProductId());
            } catch (Exception ex) {
                record.setAttempts(record.getAttempts() + 1);
                record.setStatus(1);
                record.setErrorMessage(ex.getMessage());
                record.setUpdateTime(LocalDateTime.now());
                compensationRecordMapper.updateById(record);
            }
        }
    }
}
