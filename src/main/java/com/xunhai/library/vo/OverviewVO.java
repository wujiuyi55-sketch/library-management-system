package com.xunhai.library.vo;

import lombok.Data;

/**
 * 首页统计概览。
 */
@Data
public class OverviewVO {

    /** 图书种类数 */
    private long bookCount;

    /** 可借册数 */
    private long availableStock;

    /** 读者数 */
    private long readerCount;

    /** 当前在借（含超期）记录数 */
    private long borrowingCount;

    /** 超期未还记录数 */
    private long overdueCount;
}
