package com.seckill.order.controller;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.util.JsonUtil;
import com.seckill.order.service.SeckillOrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 支付回调接口（支付网关调用）
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentCallbackController {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private SeckillOrderService seckillOrderService;

    @PostMapping("/callback")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallbackReq req) {
        try {
            log.info("收到支付回调: {}", JsonUtil.toJsonString(req));

            // Basic validation
            if (req.getOrderId() == null) {
                return ResponseEntity.badRequest().body("missing orderId");
            }

            // Publish payment success event to Kafka for consumers to process
            String msg = JSON.toJSONString(req);
            kafkaTemplate.send(SeckillConstants.TOPIC_PAYMENT_SUCCESS, msg);

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
        private String status; // e.g., SUCCESS
    }
}
