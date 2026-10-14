package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.PaymentCallbackLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentCallbackLogMapper extends BaseMapper<PaymentCallbackLog> {

    @Select("SELECT COUNT(1) FROM payment_callback_log WHERE order_id = #{orderId} AND transaction_id = #{transactionId}")
    int countByOrderAndTransaction(@Param("orderId") Long orderId,
                                   @Param("transactionId") String transactionId);

    @Insert("INSERT IGNORE INTO payment_callback_log (order_id, transaction_id, status, payload, created_at, processed_at) VALUES (#{orderId}, #{transactionId}, #{status}, #{payload}, NOW(), NOW())")
    int insertIfAbsent(@Param("orderId") Long orderId,
                       @Param("transactionId") String transactionId,
                       @Param("status") String status,
                       @Param("payload") String payload);
}
