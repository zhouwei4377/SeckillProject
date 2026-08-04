package com.seckill.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.payment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
    
    /**
     * 根据订单ID查询支付记录
     */
    PaymentRecord selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 更新支付状态 (CAS乐观锁)
     */
    @Update("UPDATE t_payment_record SET status = #{newStatus}, payment_time = #{paymentTime}, " +
            "version = version + 1, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{oldStatus} AND version = #{version}")
    int updatePaymentStatusByVersion(@Param("id") Long id,
                                      @Param("oldStatus") Integer oldStatus,
                                      @Param("newStatus") Integer newStatus,
                                      @Param("paymentTime") java.time.LocalDateTime paymentTime,
                                      @Param("version") Integer version);
}
