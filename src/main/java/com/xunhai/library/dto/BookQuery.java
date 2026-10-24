package com.xunhai.library.dto;

import lombok.Data;

/**
 * 图书检索条件。
 */
@Data
public class BookQuery {

    /** 关键字：书名 / 作者 / ISBN 模糊匹配 */
    private String keyword;

    private Long categoryId;

    /** 只看有库存的 */
    private Boolean onlyAvailable;

    private Integer page = 1;

    private Integer size = 10;

    /** MyBatis 通过 getter 读取，计算 LIMIT 偏移量 */
    public int getOffset() {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        return (p - 1) * s;
    }
}
