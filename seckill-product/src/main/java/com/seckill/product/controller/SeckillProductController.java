package com.seckill.product.controller;

import com.seckill.common.model.ApiResponse;
import com.seckill.product.dto.SeckillProductDTO;
import com.seckill.product.service.SeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 秒杀商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/seckill/product")
public class SeckillProductController {
    
    @Resource
    private SeckillProductService seckillProductService;
    
    /**
     * 获取所有秒杀商品列表
     */
    @GetMapping("/list")
    public ApiResponse<List<SeckillProductDTO>> listSeckillProducts() {
        List<SeckillProductDTO> products = seckillProductService.getAllSeckillProducts();
        return ApiResponse.success(products, "获取秒杀商品列表成功");
    }
    
    /**
     * 获取秒杀商品详情
     */
    @GetMapping("/detail/{productId}")
    public ApiResponse<SeckillProductDTO> getSeckillProductDetail(
            @PathVariable Long productId) {
        SeckillProductDTO product = seckillProductService.getSeckillProductDetail(productId);
        return ApiResponse.success(product, "获取商品详情成功");
    }
    
    /**
     * 预热缓存 (内部接口)
     */
    @PostMapping("/warmup")
    public ApiResponse<String> warmUpCache() {
        seckillProductService.warmUpCache();
        return ApiResponse.success("缓存预热完成");
    }
}
