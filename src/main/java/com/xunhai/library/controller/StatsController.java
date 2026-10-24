package com.xunhai.library.controller;

import com.xunhai.library.common.Result;
import com.xunhai.library.service.StatsService;
import com.xunhai.library.vo.CategoryStatVO;
import com.xunhai.library.vo.HotBookVO;
import com.xunhai.library.vo.MonthlyStatVO;
import com.xunhai.library.vo.OverdueVO;
import com.xunhai.library.vo.OverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计接口：概览、热门图书、分类分布、月度借阅、超期明细。
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.ok(statsService.overview());
    }

    @GetMapping("/hot-books")
    public Result<List<HotBookVO>> hotBooks(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(statsService.hotBooks(limit));
    }

    @GetMapping("/categories")
    public Result<List<CategoryStatVO>> categories() {
        return Result.ok(statsService.categoryStats());
    }

    @GetMapping("/monthly")
    public Result<List<MonthlyStatVO>> monthly(@RequestParam(defaultValue = "6") int months) {
        return Result.ok(statsService.monthlyStats(months));
    }

    @GetMapping("/overdue")
    public Result<List<OverdueVO>> overdue(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(statsService.overdueList(limit));
    }
}
