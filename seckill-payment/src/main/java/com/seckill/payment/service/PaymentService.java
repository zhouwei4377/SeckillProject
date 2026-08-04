package com.seckill.payment.service;

import com.seckill.payment.dto.PaymentRecordDTO;
import com.seckill.payment.dto.PaymentRequest;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 创建支付单
     */
    PaymentRecordDTO createPayment(PaymentRequest request);
    
    /**
     * 支付 (核心业务)
     */
    boolean pay(Long paymentId, String paymentPassword);
    
    /**
     * 查询支付记录
     */
    PaymentRecordDTO getPaymentRecord(Long paymentId);
    
    /**
     * 根据订单ID查询支付记录
     */
    PaymentRecordDTO getPaymentByOrderId(Long orderId);
    
    /**
     * 支付超时自动取消
     */
    int cancelExpiredPayments();
}
