package com.seckill.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页记录数
     */
    private Integer pageSize;
    
    /**
     * 当前页数据
     */
    private List<T> data;
    
    public static <T> PageResult<T> empty() {
        return PageResult.<T>builder()
            .total(0L)
            .totalPages(0)
            .pageNum(1)
            .pageSize(10)
            .data(Collections.emptyList())
            .build();
    }
    
    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> data) {
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        return PageResult.<T>builder()
            .total(total)
            .totalPages(totalPages)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .data(data)
            .build();
    }
}
