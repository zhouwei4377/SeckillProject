package com.seckill.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.product.entity.StockChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存变更日志Mapper
 */
@Mapper
public interface StockChangeLogMapper extends BaseMapper<StockChangeLog> {
}
