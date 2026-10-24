package com.xunhai.library.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 超期未还明细。超期天数与罚金在应用层按「当前时间 - 应还时间」计算，
 * 避免依赖不同数据库的日期函数。
 */
@Data
public class OverdueVO {

    private Long recordId;

    private String bookTitle;

    private String readerName;

    private String readerNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueTime;

    private long overdueDays;

    private BigDecimal fine;
}
