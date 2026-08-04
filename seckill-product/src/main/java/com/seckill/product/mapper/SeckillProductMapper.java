package com.seckill.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.product.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 秒杀商品Mapper
 */
@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {
    
    /**
     * 查询所有秒杀商品
     */
    List<SeckillProduct> selectAllSeckillProducts();
    
    /**
     * 通过版本号更新库存 (CAS乐观锁)
     */
    @Update("UPDATE t_seckill_product SET current_stock = current_stock - #{quantity}, " +
            "sold_quantity = sold_quantity + #{quantity}, stock_version = stock_version + 1 " +
            "WHERE id = #{productId} AND stock_version = #{version} AND current_stock >= #{quantity}")
    int decreaseStockByVersion(@Param("productId") Long productId,
                               @Param("quantity") Integer quantity,
                               @Param("version") Integer version);
    
    /**
     * 恢复库存 (用于订单取消)
     */
    @Update("UPDATE t_seckill_product SET current_stock = current_stock + #{quantity}, " +
            "sold_quantity = sold_quantity - #{quantity} " +
            "WHERE id = #{productId} AND current_stock + #{quantity} <= initial_stock")
    int increaseStock(@Param("productId") Long productId,
                      @Param("quantity") Integer quantity);
}
