package com.seckill.order.service.impl;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.exception.SeckillException;
import com.seckill.common.util.IdGenerator;
import com.seckill.common.util.JsonUtil;
import com.seckill.order.dto.ParticipateSecKillRequest;
import com.seckill.order.dto.SeckillOrderDTO;
import com.seckill.order.entity.SeckillOrder;
import com.seckill.order.mapper.SeckillOrderMapper;
import com.seckill.order.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单服务实现
 * 核心业务逻辑：验证 -> 去重 -> 扣减库存 -> 创建订单
 */
@Slf4j
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    
    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private RedissonClient redissonClient;
    
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillOrderDTO participateInSeckill(ParticipateSecKillRequest request) {
        // 参数验证
        validateRequest(request);
        
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        
        try {
            // 第一步：检查幂等性 (防需重帐)
            if (!ensureIdempotency(userId, productId, request.getRequestId())) {
                throw SeckillException.duplicateRequest();
            }
            
            // 第二步：检查用户是否超过秒杀限制
            if (!checkUserSeckillLimit(userId)) {
                throw SeckillException.rateLimitExceeded();
            }
            
            // 第三步：扣减库存 (调用商品服务)
            // TODO: 通过Feign调用商品服务扣减库存
            boolean decreaseStockSuccess = decreaseStockRemotely(productId, quantity);
            if (!decreaseStockSuccess) {
                throw SeckillException.stockInsufficient();
            }
            
            // 第四步：创建订单
            SeckillOrder order = createOrder(userId, productId, quantity);
            
            // 第五步：发送消息氺列 (异步处理)
            publishOrderCreatedEvent(order);
            
            log.info("秒杀订单创建成功: userId={}, productId={}, orderNo={}",
                userId, productId, order.getOrderNo());
            
            return convertToDTO(order);
            
        } catch (SeckillException e) {
            throw e;
        } catch (Exception e) {
            log.error("秒杀订单创建异常: userId={}, productId={}", userId, productId, e);
            throw SeckillException.systemError("秒杀失败，请稍后重试");
        }
    }
    
    @Override
    public SeckillOrderDTO getOrderDetail(Long orderId) {
        SeckillOrder order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw SeckillException.invalidParameter("订单不存在");
        }
        return convertToDTO(order);
    }
    
    @Override
    public SeckillOrderDTO getOrderByOrderNo(String orderNo) {
        SeckillOrder order = seckillOrderMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw SeckillException.invalidParameter("订单不存在");
        }
        return convertToDTO(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        SeckillOrder order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw SeckillException.invalidParameter("订单不存在");
        }
        
        // 只有待支付订单才能取消
        if (order.getStatus() != SeckillConstants.ORDER_STATUS_PENDING) {
            throw SeckillException.invalidParameter("仅待支付订单可以取消");
        }
        
        try {
            // 恢复库存
            restoreStockRemotely(order.getProductId(), order.getQuantity());
            
            // 更新订单状态
            order.setStatus(SeckillConstants.ORDER_STATUS_CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            seckillOrderMapper.updateById(order);
            
            // 发送订单取消事件
            publishOrderCancelledEvent(order);
            
            log.info("订单取消成功: orderId={}, orderNo={}", orderId, order.getOrderNo());
            return true;
            
        } catch (Exception e) {
            log.error("订单取消异常: orderId={}", orderId, e);
            throw SeckillException.systemError("订单取消失败");
        }
    }
    
    @Override
    public int cancelExpiredOrders() {
        // 查询所有超时未支付的订单
        LocalDateTime expiredTime = LocalDateTime.now()
            .minusMinutes(SeckillConstants.ORDER_TIMEOUT_MINUTES);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillOrder> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getStatus, SeckillConstants.ORDER_STATUS_PENDING)
                .lt(SeckillOrder::getCreateTime, expiredTime);
        
        java.util.List<SeckillOrder> expiredOrders = seckillOrderMapper.selectList(wrapper);
        
        int cancelCount = 0;
        for (SeckillOrder order : expiredOrders) {
            try {
                cancelOrder(order.getId());
                cancelCount++;
            } catch (Exception e) {
                log.warn("自动取消订单失败: orderId={}", order.getId(), e);
            }
        }
        
        log.info("自动取消订单算案完成: totalCount={}, cancelCount={}", 
            expiredOrders.size(), cancelCount);
        
        return cancelCount;
    }
    
    /**
     * 验证请求参数
     */
    private void validateRequest(ParticipateSecKillRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw SeckillException.invalidParameter("用户ID无效");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw SeckillException.invalidParameter("商品ID无效");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw SeckillException.invalidParameter("数量无效");
        }
    }
    
    /**
     * 检查幂等性 (防止重复订单)
     */
    private boolean ensureIdempotency(Long userId, Long productId, String requestId) {
        String key = SeckillConstants.USER_PARTICIPATE_KEY + userId + ":" + productId;
        
        // 检查是否已经申请过
        Object existingRequestId = redisTemplate.opsForValue().get(key);
        if (existingRequestId != null) {
            return false; // 已程氯隔输轨过
        }
        
        // 记录本次请求ID (略过一段时间，預設为2分钟)
        String idToStore = requestId != null ? requestId : IdGenerator.generateUUID();
        redisTemplate.opsForValue().set(
            key,
            idToStore,
            2,
            TimeUnit.MINUTES
        );
        
        return true;
    }
    
    /**
     * 检查用户是否超过限制
     */
    private boolean checkUserSeckillLimit(Long userId) {
        String key = SeckillConstants.USER_SECKILL_ATTEMPT_LIMIT + userId;
        Object attemptCount = redisTemplate.opsForValue().get(key);
        
        int count = attemptCount == null ? 0 : Integer.parseInt(attemptCount.toString());
        if (count >= SeckillConstants.USER_SECKILL_ATTEMPT_LIMIT) {
            return false; // 超过限制
        }
        
        // 绺驟计沕
        redisTemplate.opsForValue().increment(key);
        if (count == 0) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        
        return true;
    }
    
    /**
     * 遠程扣减库存 (Feign)
     */
    private boolean decreaseStockRemotely(Long productId, Integer quantity) {
        // TODO: 通过Feign调用商品服务
        // FeignClient productServiceClient.decreaseStock(productId, quantity)
        return true;
    }
    
    /**
     * 遠程恢复库存 (Feign)
     */
    private boolean restoreStockRemotely(Long productId, Integer quantity) {
        // TODO: 通过Feign调用商品服务
        // FeignClient productServiceClient.restoreStock(productId, quantity)
        return true;
    }
    
    /**
     * 创建订单
     */
    private SeckillOrder createOrder(Long userId, Long productId, Integer quantity) {
        // TODO: 从商品服务获取商品秒杀价
        BigDecimal seckillPrice = new BigDecimal("99.99");
        BigDecimal totalPrice = seckillPrice.multiply(new BigDecimal(quantity));
        
        SeckillOrder order = SeckillOrder.builder()
            .orderNo(IdGenerator.generateOrderId())
            .userId(userId)
            .productId(productId)
            .quantity(quantity)
            .seckillPrice(seckillPrice)
            .totalPrice(totalPrice)
            .status(SeckillConstants.ORDER_STATUS_PENDING)
            .createTime(LocalDateTime.now())
            .version(0)
            .deleted(0)
            .build();
        
        seckillOrderMapper.insert(order);
        return order;
    }
    
    /**
     * 发布订单创建次次事件
     */
    private void publishOrderCreatedEvent(SeckillOrder order) {
        try {
            String message = JsonUtil.toJsonString(order);
            kafkaTemplate.send(SeckillConstants.TOPIC_ORDER_CREATE, message);
        } catch (Exception e) {
            log.warn("发布订单创建事件失败: orderId={}", order.getId(), e);
        }
    }
    
    /**
     * 发布订单取消事件
     */
    private void publishOrderCancelledEvent(SeckillOrder order) {
        try {
            String message = JsonUtil.toJsonString(order);
            kafkaTemplate.send(SeckillConstants.TOPIC_ORDER_CANCEL, message);
        } catch (Exception e) {
            log.warn("发布订单取消事件失败: orderId={}", order.getId(), e);
        }
    }
    
    /**
     * 转换为DTO
     */
    private SeckillOrderDTO convertToDTO(SeckillOrder order) {
        if (order == null) {
            return null;
        }
        return SeckillOrderDTO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .userId(order.getUserId())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .seckillPrice(order.getSeckillPrice())
            .totalPrice(order.getTotalPrice())
            .status(order.getStatus())
            .paymentMethod(order.getPaymentMethod())
            .phone(order.getPhone())
            .receiverName(order.getReceiverName())
            .deliveryAddress(order.getDeliveryAddress())
            .createTime(order.getCreateTime())
            .paymentTime(order.getPaymentTime())
            .build();
    }
}
