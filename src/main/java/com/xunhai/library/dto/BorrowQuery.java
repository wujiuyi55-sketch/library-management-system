package com.xunhai.library.dto;

import lombok.Data;

/**
 * 借阅记录检索条件。
 */
@Data
public class BorrowQuery {

    /** BORROWED / OVERDUE / RETURNED，为空查全部 */
    private String status;

    private Long readerId;

    private Long bookId;

    private Integer page = 1;

    private Integer size = 10;

    public int getOffset() {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        return (p - 1) * s;
    }
}
