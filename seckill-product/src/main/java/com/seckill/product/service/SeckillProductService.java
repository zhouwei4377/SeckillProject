package com.seckill.product.service;

import com.seckill.product.dto.SeckillProductDTO;
import com.seckill.product.entity.SeckillProduct;

import java.util.List;

/**
 * 秒杀商品服务接口
 */
public interface SeckillProductService {
    
    /**
     * 获取所有秒杀商品列表
     */
    List<SeckillProductDTO> getAllSeckillProducts();
    
    /**
     * 获取秒杀商品详情
     */
    SeckillProductDTO getSeckillProductDetail(Long productId);
    
    /**
     * 扣减库存 (Redis原子操作)
     */
    boolean decreaseStock(Long productId, Integer quantity, Long orderId);
    
    /**
     * 恢复库存 (订单取消时调用)
     */
    boolean restoreStock(Long productId, Integer quantity);
    
    /**
     * 预热缓存 (服务启动时调用)
     */
    void warmUpCache();
    
    /**
     * 刷新秒杀商品状态 (检查是否已开始/已结束)
     */
    void refreshSeckillStatus();
}
