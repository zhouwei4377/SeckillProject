package com.seckill.common.model;

import com.seckill.common.constant.SeckillConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum;
    
    @Min(value = 1, message = "每页记录数最小值为1")
    private Integer pageSize;
    
    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }
    
    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        if (pageSize > SeckillConstants.MAX_PAGE_SIZE) {
            return SeckillConstants.MAX_PAGE_SIZE;
        }
        return pageSize;
    }
    
    public Integer getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }
}
