package com.seckill.order.service;

public interface PaymentCallbackLogService {

    boolean tryProcess(Long orderId, String transactionId, String status, String payload);
}
