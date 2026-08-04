package com.seckill.product.service.impl;

import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.exception.SeckillException;
import com.seckill.common.util.JsonUtil;
import com.seckill.product.dto.SeckillProductDTO;
import com.seckill.product.entity.SeckillProduct;
import com.seckill.product.entity.StockChangeLog;
import com.seckill.product.mapper.SeckillProductMapper;
import com.seckill.product.mapper.StockChangeLogMapper;
import com.seckill.product.service.SeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀商品服务实现
 * 核心库存防超卖逻辑
 */
@Slf4j
@Service
public class SeckillProductServiceImpl implements SeckillProductService {
    
    @Resource
    private SeckillProductMapper seckillProductMapper;
    
    @Resource
    private StockChangeLogMapper stockChangeLogMapper;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private RedissonClient redissonClient;
    
    @Override
    public List<SeckillProductDTO> getAllSeckillProducts() {
        // 先从缓存获取
        Object cachedData = redisTemplate.opsForValue().get("seckill:product:list");
        if (cachedData != null) {
            return JsonUtil.parseArray(cachedData.toString(), SeckillProductDTO.class);
        }
        
        // 从数据库获取
        List<SeckillProduct> products = seckillProductMapper.selectAllSeckillProducts();
        List<SeckillProductDTO> dtoList = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 缓存结果 (24小时)
        redisTemplate.opsForValue().set(
            "seckill:product:list",
            JsonUtil.toJsonString(dtoList),
            SeckillConstants.CACHE_EXPIRE_HOURS,
            TimeUnit.HOURS
        );
        
        return dtoList;
    }
    
    @Override
    public SeckillProductDTO getSeckillProductDetail(Long productId) {
        String cacheKey = SeckillConstants.SECKILL_PRODUCT_KEY + productId;
        
        // 从缓存获取
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return JsonUtil.parseObject(cachedData.toString(), SeckillProductDTO.class);
        }
        
        // 从数据库获取
        SeckillProduct product = seckillProductMapper.selectById(productId);
        if (product == null) {
            throw SeckillException.invalidParameter("商品不存在");
        }
        
        SeckillProductDTO dto = convertToDTO(product);
        
        // 缓存商品信息
        redisTemplate.opsForValue().set(
            cacheKey,
            JsonUtil.toJsonString(dto),
            SeckillConstants.CACHE_EXPIRE_HOURS,
            TimeUnit.HOURS
        );
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long productId, Integer quantity, Long orderId) {
        if (quantity <= 0) {
            throw SeckillException.invalidParameter("数量必须大于0");
        }
        
        try {
            // 第一步：Redis快速路径 (原子操作)
            String stockKey = SeckillConstants.PRODUCT_STOCK_KEY + productId;
            Long remainStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            
            if (remainStock < 0) {
                // Redis库存不足，回滚
                redisTemplate.opsForValue().increment(stockKey, quantity);
                throw SeckillException.stockInsufficient();
            }
            
            // 第二步：库存预警检查
            SeckillProduct product = seckillProductMapper.selectById(productId);
            if (product == null) {
                throw SeckillException.invalidParameter("商品不存在");
            }
            
            int warningStock = (int) (product.getInitialStock() * 
                SeckillConstants.STOCK_WARNING_PERCENTAGE / 100);
            if (remainStock < warningStock) {
                log.warn("商品库存预警：productId={}, remainStock={}", productId, remainStock);
            }
            
            // 第三步：数据库CAS更新 (二次验证)
            int version = product.getStockVersion();
            int updateCount = seckillProductMapper.decreaseStockByVersion(
                productId, quantity, version
            );
            
            if (updateCount == 0) {
                // 数据库更新失败（版本号不匹配或库存不足）
                redisTemplate.opsForValue().increment(stockKey, quantity);
                throw SeckillException.stockInsufficient();
            }
            
            // 第四步：记录库存变更日志
            recordStockChange(productId, quantity, product.getCurrentStock(), 
                product.getCurrentStock() - quantity, 1, orderId);
            
            log.info("库存扣减成功：productId={}, quantity={}, orderId={}", 
                productId, quantity, orderId);
            
            return true;
            
        } catch (SeckillException e) {
            throw e;
        } catch (Exception e) {
            log.error("库存扣减异常：productId={}, quantity={}", productId, quantity, e);
            throw SeckillException.systemError("库存扣减失败，请稍后重试");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(Long productId, Integer quantity) {
        try {
            // 恢复Redis库存
            String stockKey = SeckillConstants.PRODUCT_STOCK_KEY + productId;
            redisTemplate.opsForValue().increment(stockKey, quantity);
            
            // 恢复数据库库存
            int updateCount = seckillProductMapper.increaseStock(productId, quantity);
            
            if (updateCount > 0) {
                SeckillProduct product = seckillProductMapper.selectById(productId);
                recordStockChange(productId, quantity, product.getCurrentStock() - quantity,
                    product.getCurrentStock(), 2, null);
                
                log.info("库存恢复成功：productId={}, quantity={}", productId, quantity);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("库存恢复异常：productId={}, quantity={}", productId, quantity, e);
            throw SeckillException.systemError("库存恢复失败");
        }
    }
    
    @Override
    public void warmUpCache() {
        log.info("开始预热缓存...");
        try {
            List<SeckillProduct> products = seckillProductMapper.selectAllSeckillProducts();
            
            for (SeckillProduct product : products) {
                // 预热商品信息缓存
                String productKey = SeckillConstants.SECKILL_PRODUCT_KEY + product.getId();
                redisTemplate.opsForValue().set(
                    productKey,
                    JsonUtil.toJsonString(convertToDTO(product)),
                    SeckillConstants.CACHE_EXPIRE_HOURS,
                    TimeUnit.HOURS
                );
                
                // 预热库存缓存
                String stockKey = SeckillConstants.PRODUCT_STOCK_KEY + product.getId();
                redisTemplate.opsForValue().set(
                    stockKey,
                    String.valueOf(product.getCurrentStock()),
                    SeckillConstants.CACHE_EXPIRE_HOURS,
                    TimeUnit.HOURS
                );
            }
            
            log.info("缓存预热完成，共预热 {} 个商品", products.size());
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    @Override
    public void refreshSeckillStatus() {
        // TODO: 定时刷新秒杀商品的状态
    }
    
    /**
     * 转换为DTO
     */
    private SeckillProductDTO convertToDTO(SeckillProduct product) {
        if (product == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        int status = product.getStatus();
        
        // 检查秒杀状态
        if (now.isBefore(product.getStartTime())) {
            status = SeckillConstants.SECKILL_STATUS_NOT_START;
        } else if (now.isAfter(product.getEndTime())) {
            status = SeckillConstants.SECKILL_STATUS_ENDED;
        } else if (product.getCurrentStock() <= 0) {
            status = SeckillConstants.SECKILL_STATUS_SOLD_OUT;
        } else {
            status = SeckillConstants.SECKILL_STATUS_IN_PROGRESS;
        }
        
        // 计算折扣率
        double discountRate = product.getSeckillPrice().doubleValue() / 
            product.getOriginalPrice().doubleValue() * 100;
        
        // 计算剩余时间
        int remainingTime = 0;
        if (status == SeckillConstants.SECKILL_STATUS_IN_PROGRESS) {
            remainingTime = (int) java.time.temporal.ChronoUnit.SECONDS.between(now, product.getEndTime());
        }
        
        return SeckillProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .originalPrice(product.getOriginalPrice())
            .seckillPrice(product.getSeckillPrice())
            .initialStock(product.getInitialStock())
            .currentStock(product.getCurrentStock())
            .soldQuantity(product.getSoldQuantity())
            .startTime(product.getStartTime())
            .endTime(product.getEndTime())
            .status(status)
            .imageUrl(product.getImageUrl())
            .discountRate(discountRate)
            .remainingTime(remainingTime)
            .isSoldOut(product.getCurrentStock() <= 0)
            .build();
    }
    
    /**
     * 记录库存变更日志
     */
    private void recordStockChange(Long productId, Integer quantity,
                                    Integer stockBefore, Integer stockAfter,
                                    Integer changeType, Long orderId) {
        try {
            StockChangeLog log = StockChangeLog.builder()
                .productId(productId)
                .changeType(changeType)
                .changeQuantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .orderId(orderId)
                .operator("SYSTEM")
                .createTime(LocalDateTime.now())
                .build();
            
            stockChangeLogMapper.insert(log);
        } catch (Exception e) {
            logger.warn("记录库存变更日志失败", e);
        }
    }
}
