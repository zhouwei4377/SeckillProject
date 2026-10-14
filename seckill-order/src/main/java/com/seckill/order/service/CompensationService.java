package com.seckill.order.service;

public interface CompensationService {

    void createCompensation(Long orderId, Long productId, Integer quantity, String reason);

    void retryPendingCompensations();
}
