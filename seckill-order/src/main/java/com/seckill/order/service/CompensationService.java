package com.seckill.order.service;

public interface CompensationService {
    void submitRestoreStockCompensation(Long orderId, Long productId, Integer quantity);
}
