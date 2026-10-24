package com.xunhai.library.domain;

import lombok.Data;

/**
 * 图书分类。
 */
@Data
public class Category {

    private Long id;

    private String name;

    private Integer sort;
}
