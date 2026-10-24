package com.xunhai.library.service;

import com.xunhai.library.vo.CategoryStatVO;
import com.xunhai.library.vo.HotBookVO;
import com.xunhai.library.vo.MonthlyStatVO;
import com.xunhai.library.vo.OverdueVO;
import com.xunhai.library.vo.OverviewVO;

import java.util.List;

/**
 * 统计业务。
 */
public interface StatsService {

    OverviewVO overview();

    List<HotBookVO> hotBooks(int limit);

    List<CategoryStatVO> categoryStats();

    /** 最近 N 个月的借阅量，没有数据的月份补 0 */
    List<MonthlyStatVO> monthlyStats(int months);

    List<OverdueVO> overdueList(int limit);
}
