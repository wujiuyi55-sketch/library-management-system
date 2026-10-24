package com.xunhai.library.vo;

import lombok.Data;

/**
 * 月度借阅量。
 */
@Data
public class MonthlyStatVO {

    private int year;

    private int month;

    private long total;
}
