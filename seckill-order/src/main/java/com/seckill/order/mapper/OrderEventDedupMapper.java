package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.OrderEventDedup;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderEventDedupMapper extends BaseMapper<OrderEventDedup> {

    @Insert("INSERT IGNORE INTO order_event_dedup (event_type, aggregate_id, created_at) VALUES (#{eventType}, #{aggregateId}, NOW())")
    int insertIfAbsent(@Param("eventType") String eventType,
                       @Param("aggregateId") String aggregateId);

    @Select("SELECT COUNT(1) FROM order_event_dedup WHERE event_type = #{eventType} AND aggregate_id = #{aggregateId}")
    int countByEventTypeAndAggregateId(@Param("eventType") String eventType,
                                      @Param("aggregateId") String aggregateId);
}
