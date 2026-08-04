package com.seckill.payment.controller;

import com.seckill.common.model.ApiResponse;
import com.seckill.payment.dto.PaymentRecordDTO;
import com.seckill.payment.dto.PaymentRequest;
import com.seckill.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 支付服务控制器
 */
@Slf4j
@RestController
@RequestMapping("/seckill/payment")
@Validated
public class PaymentController {
    
    @Resource
    private PaymentService paymentService;
    
    /**
     * 创建支付单
     */
    @PostMapping("/create")
    public ApiResponse<PaymentRecordDTO> createPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentRecordDTO payment = paymentService.createPayment(request);
        return ApiResponse.success(payment, "支付单创建成功");
    }
    
    /**
     * 执行支付
     */
    @PostMapping("/{paymentId}/pay")
    public ApiResponse<String> pay(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String paymentPassword) {
        boolean result = paymentService.pay(paymentId, paymentPassword);
        return ApiResponse.success("支付成功");
    }
    
    /**
     * 查询支付记录
     */
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentRecordDTO> getPaymentRecord(
            @PathVariable Long paymentId) {
        PaymentRecordDTO payment = paymentService.getPaymentRecord(paymentId);
        return ApiResponse.success(payment, "获取支付记录成功");
    }
    
    /**
     * 根据订单ID查询支付记录
     */
    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentRecordDTO> getPaymentByOrderId(
            @PathVariable Long orderId) {
        PaymentRecordDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ApiResponse.success(payment, "获取支付记录成功");
    }
}
