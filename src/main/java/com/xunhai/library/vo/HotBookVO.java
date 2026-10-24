package com.xunhai.library.vo;

import lombok.Data;

/**
 * 热门图书（按借阅次数排序）。
 */
@Data
public class HotBookVO {

    private Long bookId;

    private String title;

    private String author;

    private long borrowCount;
}
