package com.seckill.product.controller;

import com.seckill.product.service.SeckillProductService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 内部调用的商品相关接口（服务间调用）
 */
@Slf4j
@RestController
@RequestMapping("/internal/product")
public class InternalProductController {

    @Resource
    private SeckillProductService seckillProductService;

    @PostMapping("/{productId}/decreaseStock")
    public ResponseEntity<Boolean> decreaseStock(@PathVariable("productId") Long productId,
                                                 @RequestBody DecreaseStockRequest req) {
        try {
            boolean ok = seckillProductService.decreaseStock(productId, req.getQuantity(), req.getOrderId());
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            log.warn("内部扣减库存异常 productId={}, quantity={}, err={}", productId, req.getQuantity(), e.getMessage());
            return ResponseEntity.status(500).body(false);
        }
    }

    @Data
    public static class DecreaseStockRequest {
        private Integer quantity;
        private Long orderId;
    }
}
