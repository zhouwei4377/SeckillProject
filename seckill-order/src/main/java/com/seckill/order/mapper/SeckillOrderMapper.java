package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 秒杀订单Mapper
 */
@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    
    /**
     * 查询用户是否已经訂項進行中
     */
    @Select("SELECT COUNT(*) FROM t_seckill_order " +
            "WHERE user_id = #{userId} AND product_id = #{productId} " +
            "AND status IN (1, 2, 3, 4)")
    int countUserProductOrders(@Param("userId") Long userId,
                               @Param("productId") Long productId);
    
    /**
     * 新建订单
     */
    int insertOrder(SeckillOrder order);
}
