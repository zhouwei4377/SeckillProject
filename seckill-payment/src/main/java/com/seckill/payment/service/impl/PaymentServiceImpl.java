package com.seckill.payment.service.impl;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.exception.SeckillException;
import com.seckill.common.util.IdGenerator;
import com.seckill.common.util.JsonUtil;
import com.seckill.payment.dto.PaymentRecordDTO;
import com.seckill.payment.dto.PaymentRequest;
import com.seckill.payment.entity.PaymentRecord;
import com.seckill.payment.mapper.PaymentRecordMapper;
import com.seckill.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现
 * 核心逻辑：创建支付单 -> 检查订单状态 -> 处理支付 -> 发布事件 -> 更新订单
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecordDTO createPayment(PaymentRequest request) {
        // 参数验证
        validatePaymentRequest(request);
        
        try {
            // 创建支付记录
            PaymentRecord record = PaymentRecord.builder()
                .paymentNo(IdGenerator.generatePaymentId())
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentChannel(request.getPaymentChannel())
                .status(SeckillConstants.PAYMENT_STATUS_PENDING)
                .version(0)
                .deleted(0)
                .createTime(LocalDateTime.now())
                .build();
            
            paymentRecordMapper.insert(record);
            
            // 在Redis中记录支付单信息 (用于支付超时检查)
            String paymentKey = "payment:pending:" + record.getId();
            redisTemplate.opsForValue().set(
                paymentKey,
                record.getId().toString(),
                SeckillConstants.PAYMENT_TIMEOUT_MINUTES,
                TimeUnit.MINUTES
            );
            
            log.info("支付单创建成功: paymentNo={}, orderId={}, amount={}",
                record.getPaymentNo(), record.getOrderId(), record.getAmount());
            
            return convertToDTO(record);
            
        } catch (Exception e) {
            log.error("创建支付单异常: orderId={}", request.getOrderId(), e);
            throw SeckillException.systemError("创建支付单失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pay(Long paymentId, String paymentPassword) {
        if (paymentId == null || paymentId <= 0) {
            throw SeckillException.invalidParameter("支付单ID无效");
        }
        
        try {
            // 获取支付记录
            PaymentRecord record = paymentRecordMapper.selectById(paymentId);
            if (record == null) {
                throw SeckillException.invalidParameter("支付单不存在");
            }
            
            // 检查支付单状态
            if (record.getStatus() != SeckillConstants.PAYMENT_STATUS_PENDING) {
                throw SeckillException.invalidParameter("支付单已处理");
            }
            
            // 检查支付超时 (超过30分钟自动失败)
            LocalDateTime expiredTime = record.getCreateTime()
                .plusMinutes(SeckillConstants.PAYMENT_TIMEOUT_MINUTES);
            if (LocalDateTime.now().isAfter(expiredTime)) {
                // 自动取消支付单
                record.setStatus(SeckillConstants.PAYMENT_STATUS_FAILED);
                paymentRecordMapper.updateById(record);
                throw SeckillException.invalidParameter("支付超时");
            }
            
            // 调用第三方支付接口 (模拟)
            boolean paymentSuccess = processPaymentWithThirdParty(record, paymentPassword);
            if (!paymentSuccess) {
                throw SeckillException.invalidParameter("支付失败");
            }
            
            // 更新支付状态 (CAS乐观锁)
            int updateCount = paymentRecordMapper.updatePaymentStatusByVersion(
                paymentId,
                SeckillConstants.PAYMENT_STATUS_PENDING,
                SeckillConstants.PAYMENT_STATUS_SUCCESS,
                LocalDateTime.now(),
                record.getVersion()
            );
            
            if (updateCount == 0) {
                throw SeckillException.invalidParameter("支付单状态冲突");
            }
            
            // 清除Redis中的待支付记录
            redisTemplate.delete("payment:pending:" + paymentId);
            
            // 发布支付成功事件
            publishPaymentSuccessEvent(record);
            
            log.info("支付成功: paymentNo={}, orderId={}, amount={}",
                record.getPaymentNo(), record.getOrderId(), record.getAmount());
            
            return true;
            
        } catch (SeckillException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付异常: paymentId={}", paymentId, e);
            throw SeckillException.systemError("支付异常");
        }
    }
    
    @Override
    public PaymentRecordDTO getPaymentRecord(Long paymentId) {
        PaymentRecord record = paymentRecordMapper.selectById(paymentId);
        if (record == null) {
            throw SeckillException.invalidParameter("支付单不存在");
        }
        return convertToDTO(record);
    }
    
    @Override
    public PaymentRecordDTO getPaymentByOrderId(Long orderId) {
        PaymentRecord record = paymentRecordMapper.selectByOrderId(orderId);
        if (record == null) {
            return null;
        }
        return convertToDTO(record);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelExpiredPayments() {
        // 查询所有待支付且超时的支付单
        LocalDateTime expiredTime = LocalDateTime.now()
            .minusMinutes(SeckillConstants.PAYMENT_TIMEOUT_MINUTES);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
                .eq(PaymentRecord::getStatus, SeckillConstants.PAYMENT_STATUS_PENDING)
                .lt(PaymentRecord::getCreateTime, expiredTime);
        
        List<PaymentRecord> expiredRecords = paymentRecordMapper.selectList(wrapper);
        
        int cancelCount = 0;
        for (PaymentRecord record : expiredRecords) {
            try {
                // 标记为支付失败
                record.setStatus(SeckillConstants.PAYMENT_STATUS_FAILED);
                record.setRemark("支付超时，自动取消");
                paymentRecordMapper.updateById(record);
                
                // 清除Redis中的待支付记录
                redisTemplate.delete("payment:pending:" + record.getId());
                
                // 发布支付失败事件
                publishPaymentFailedEvent(record);
                
                cancelCount++;
            } catch (Exception e) {
                log.warn("自动取消支付单异常: paymentId={}", record.getId(), e);
            }
        }
        
        log.info("支付超时取消任务完成: totalCount={}, cancelCount={}",
            expiredRecords.size(), cancelCount);
        
        return cancelCount;
    }
    
    /**
     * 验证支付请求参数
     */
    private void validatePaymentRequest(PaymentRequest request) {
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw SeckillException.invalidParameter("订单ID无效");
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw SeckillException.invalidParameter("用户ID无效");
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw SeckillException.invalidParameter("支付金额无效");
        }
        if (request.getPaymentMethod() == null) {
            throw SeckillException.invalidParameter("支付方式不能为空");
        }
    }
    
    /**
     * 调用第三方支付接口 (模拟)
     */
    private boolean processPaymentWithThirdParty(PaymentRecord record, String paymentPassword) {
        try {
            // TODO: 调用实际的第三方支付接口
            // 这里模拟支付成功
            log.info("调用第三方支付接口: paymentNo={}, paymentChannel={}",
                record.getPaymentNo(), record.getPaymentChannel());
            
            // 生成第三方支付单号
            record.setThirdPartyPaymentNo(IdGenerator.generateUUID());
            
            return true;
        } catch (Exception e) {
            log.error("第三方支付接口调用异常: paymentNo={}", record.getPaymentNo(), e);
            return false;
        }
    }
    
    /**
     * 发布支付成功事件
     */
    private void publishPaymentSuccessEvent(PaymentRecord record) {
        try {
            String message = JsonUtil.toJsonString(record);
            kafkaTemplate.send(SeckillConstants.TOPIC_PAYMENT_SUCCESS, message);
        } catch (Exception e) {
            log.warn("发布支付成功事件异常: paymentId={}", record.getId(), e);
        }
    }
    
    /**
     * 发布支付失败事件
     */
    private void publishPaymentFailedEvent(PaymentRecord record) {
        try {
            String message = JsonUtil.toJsonString(record);
            // TODO: 发送到支付失败主题
            log.info("发布支付失败事件: paymentId={}", record.getId());
        } catch (Exception e) {
            log.warn("发布支付失败事件异常: paymentId={}", record.getId(), e);
        }
    }
    
    /**
     * 转换为DTO
     */
    private PaymentRecordDTO convertToDTO(PaymentRecord record) {
        if (record == null) {
            return null;
        }
        return PaymentRecordDTO.builder()
            .id(record.getId())
            .paymentNo(record.getPaymentNo())
            .orderId(record.getOrderId())
            .orderNo(record.getOrderNo())
            .userId(record.getUserId())
            .amount(record.getAmount())
            .paymentMethod(record.getPaymentMethod())
            .status(record.getStatus())
            .paymentChannel(record.getPaymentChannel())
            .remark(record.getRemark())
            .createTime(record.getCreateTime())
            .paymentTime(record.getPaymentTime())
            .build();
    }
}
