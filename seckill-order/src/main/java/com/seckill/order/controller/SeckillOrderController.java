package com.seckill.order.controller;

import com.seckill.common.model.ApiResponse;
import com.seckill.order.dto.ParticipateSecKillRequest;
import com.seckill.order.dto.SeckillOrderDTO;
import com.seckill.order.service.SeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 秒杀订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/seckill/order")
@Validated
public class SeckillOrderController {
    
    @Resource
    private SeckillOrderService seckillOrderService;
    
    /**
     * 参加秒杀 (核心接口)
     */
    @PostMapping("/participate")
    public ApiResponse<SeckillOrderDTO> participateInSeckill(
            @Valid @RequestBody ParticipateSecKillRequest request) {
        SeckillOrderDTO order = seckillOrderService.participateInSeckill(request);
        return ApiResponse.success(order, "秒杀参与成功");
    }
    
    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<SeckillOrderDTO> getOrderDetail(
            @PathVariable Long orderId) {
        SeckillOrderDTO order = seckillOrderService.getOrderDetail(orderId);
        return ApiResponse.success(order, "获取订单详情成功");
    }
    
    /**
     * 查询订单状态按订单号
     */
    @GetMapping("/status/{orderNo}")
    public ApiResponse<SeckillOrderDTO> getOrderByOrderNo(
            @PathVariable String orderNo) {
        SeckillOrderDTO order = seckillOrderService.getOrderByOrderNo(orderNo);
        return ApiResponse.success(order, "获取订单详情成功");
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public ApiResponse<String> cancelOrder(
            @PathVariable Long orderId) {
        boolean result = seckillOrderService.cancelOrder(orderId);
        return ApiResponse.success("订单取消成功");
    }
}
