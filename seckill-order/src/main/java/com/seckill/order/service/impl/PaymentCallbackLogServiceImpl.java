package com.seckill.order.service.impl;

import com.seckill.order.mapper.PaymentCallbackLogMapper;
import com.seckill.order.service.PaymentCallbackLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PaymentCallbackLogServiceImpl implements PaymentCallbackLogService {

    @Resource
    private PaymentCallbackLogMapper paymentCallbackLogMapper;

    @Override
    public boolean tryProcess(Long orderId, String transactionId, String status, String payload) {
        if (orderId == null || transactionId == null) {
            return false;
        }
        return paymentCallbackLogMapper.insertIfAbsent(orderId, transactionId, status, payload) > 0;
    }
}
