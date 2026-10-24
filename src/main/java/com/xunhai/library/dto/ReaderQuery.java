package com.xunhai.library.dto;

import lombok.Data;

/**
 * 读者检索条件。
 */
@Data
public class ReaderQuery {

    /** 关键字：姓名 / 读者编号 / 手机号 模糊匹配 */
    private String keyword;

    private Integer page = 1;

    private Integer size = 10;

    public int getOffset() {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10 : size;
        return (p - 1) * s;
    }
}
