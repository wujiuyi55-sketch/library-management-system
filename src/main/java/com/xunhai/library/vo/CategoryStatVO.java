package com.xunhai.library.vo;

import lombok.Data;

/**
 * 分类借阅分布。
 */
@Data
public class CategoryStatVO {

    private String categoryName;

    private long borrowCount;
}
