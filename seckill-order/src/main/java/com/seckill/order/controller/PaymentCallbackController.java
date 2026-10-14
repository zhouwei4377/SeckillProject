package com.seckill.order.controller;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.util.JsonUtil;
import com.seckill.order.service.OutboxEventService;
import com.seckill.order.service.PaymentCallbackLogService;
import com.seckill.order.service.SeckillOrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentCallbackController {

    @Resource
    private OutboxEventService outboxEventService;

    @Resource
    private PaymentCallbackLogService paymentCallbackLogService;

    @Resource
    private SeckillOrderService seckillOrderService;

    @PostMapping("/callback")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallbackReq req) {
        try {
            log.info("收到支付回调: {}", JsonUtil.toJsonString(req));

            if (req.getOrderId() == null || req.getTransactionId() == null || req.getStatus() == null) {
                return ResponseEntity.badRequest().body("missing required callback fields");
            }

            String normalizedStatus = req.getStatus().trim();
            if (!"SUCCESS".equalsIgnoreCase(normalizedStatus)) {
                return ResponseEntity.ok("callback status not success, ignored");
            }

            if (!paymentCallbackLogService.tryProcess(req.getOrderId(), req.getTransactionId(), normalizedStatus, JSON.toJSONString(req))) {
                log.info("支付回调重复处理，orderId={}, transactionId={}", req.getOrderId(), req.getTransactionId());
                return ResponseEntity.ok("duplicate callback, ignored");
            }

            outboxEventService.publish("payment_callback", String.valueOf(req.getOrderId()), SeckillConstants.TOPIC_PAYMENT_SUCCESS, req);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return ResponseEntity.status(500).body("error");
        }
    }

    @Data
    public static class PaymentCallbackReq {
        private Long orderId;
        private String transactionId;
        private Integer amountCents;
        private String status;
    }
}
