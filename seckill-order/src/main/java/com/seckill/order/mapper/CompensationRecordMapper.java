package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.CompensationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompensationRecordMapper extends BaseMapper<CompensationRecord> {

    List<CompensationRecord> selectPendingCompensations(@Param("status") Integer status,
                                                        @Param("maxAttempts") Integer maxAttempts);
}
