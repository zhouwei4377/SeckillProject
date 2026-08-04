package com.seckill.order.service;

import com.seckill.order.dto.ParticipateSecKillRequest;
import com.seckill.order.dto.SeckillOrderDTO;

/**
 * 秒杀订单服务接口
 */
public interface SeckillOrderService {
    
    /**
     * 参加秒杀 (核心业务)
     * @param request 秒杀请求
     * @return 秒杀订单
     */
    SeckillOrderDTO participateInSeckill(ParticipateSecKillRequest request);
    
    /**
     * 查询订单详情
     */
    SeckillOrderDTO getOrderDetail(Long orderId);
    
    /**
     * 查询订单详情（按订单号）
     */
    SeckillOrderDTO getOrderByOrderNo(String orderNo);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);
    
    /**
     * 待支付订单自动取消 (定时任务调用)
     */
    int cancelExpiredOrders();
}
