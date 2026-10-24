package com.xunhai.library.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借阅记录。
 */
@Data
public class BorrowRecord {

    /** 状态：在借 */
    public static final String STATUS_BORROWED = "BORROWED";
    /** 状态：超期未还 */
    public static final String STATUS_OVERDUE = "OVERDUE";
    /** 状态：已归还 */
    public static final String STATUS_RETURNED = "RETURNED";

    private Long id;

    private Long bookId;

    private Long readerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime borrowTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    private String status;

    private Integer overdueDays;

    private BigDecimal fine;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /* ---------- 关联查询字段 ---------- */

    private String bookTitle;

    private String readerName;

    private String readerNo;
}
