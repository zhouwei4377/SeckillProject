package com.seckill.order.controller;

import com.alibaba.fastjson2.JSON;
import com.seckill.common.constant.SeckillConstants;
import com.seckill.common.util.JsonUtil;
import com.seckill.order.service.OutboxEventService;
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

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentCallbackController {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private OutboxEventService outboxEventService;

    @PostMapping("/callback")
    public ResponseEntity<String> paymentCallback(@RequestBody PaymentCallbackReq req) {
        try {
            log.info("收到支付回调: {}", JsonUtil.toJsonString(req));

            if (req.getOrderId() == null) {
                return ResponseEntity.badRequest().body("missing orderId");
            }

            String payload = JSON.toJSONString(req);
            outboxEventService.publish("payment_callback", String.valueOf(req.getOrderId()), SeckillConstants.TOPIC_PAYMENT_SUCCESS, payload);
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
