package com.xunhai.library.service.impl;

import com.xunhai.library.config.LibraryProperties;
import com.xunhai.library.mapper.StatsMapper;
import com.xunhai.library.service.StatsService;
import com.xunhai.library.vo.CategoryStatVO;
import com.xunhai.library.vo.HotBookVO;
import com.xunhai.library.vo.MonthlyStatVO;
import com.xunhai.library.vo.OverdueVO;
import com.xunhai.library.vo.OverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计业务实现。
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final LibraryProperties properties;

    @Override
    public OverviewVO overview() {
        OverviewVO vo = new OverviewVO();
        vo.setBookCount(statsMapper.countBooks());
        vo.setAvailableStock(statsMapper.sumAvailableStock());
        vo.setReaderCount(statsMapper.countReaders());
        vo.setBorrowingCount(statsMapper.countBorrowing());
        vo.setOverdueCount(statsMapper.countOverdue(LocalDateTime.now()));
        return vo;
    }

    @Override
    public List<HotBookVO> hotBooks(int limit) {
        return statsMapper.topBooks(Math.max(1, Math.min(limit, 20)));
    }

    @Override
    public List<CategoryStatVO> categoryStats() {
        return statsMapper.categoryStats();
    }

    @Override
    public List<MonthlyStatVO> monthlyStats(int months) {
        int n = Math.max(1, Math.min(months, 24));
        LocalDate firstOfThisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime start = firstOfThisMonth.minusMonths(n - 1L).atStartOfDay();

        Map<String, Long> counter = new HashMap<>();
        for (MonthlyStatVO row : statsMapper.monthlyStats(start)) {
            counter.put(row.getYear() + "-" + row.getMonth(), row.getTotal());
        }

        // 补齐没有借阅记录的月份，前端画图时 X 轴才连续
        List<MonthlyStatVO> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            LocalDate month = firstOfThisMonth.minusMonths(n - 1L - i);
            MonthlyStatVO vo = new MonthlyStatVO();
            vo.setYear(month.getYear());
            vo.setMonth(month.getMonthValue());
            vo.setTotal(counter.getOrDefault(month.getYear() + "-" + month.getMonthValue(), 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<OverdueVO> overdueList(int limit) {
        LocalDateTime now = LocalDateTime.now();
        List<OverdueVO> list = statsMapper.overdueList(now, Math.max(1, Math.min(limit, 50)));
        for (OverdueVO vo : list) {
            long days = Math.max(0, ChronoUnit.DAYS.between(vo.getDueTime(), now));
            vo.setOverdueDays(days);
            vo.setFine(properties.getFinePerDay()
                    .multiply(BigDecimal.valueOf(days))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        return list;
    }
}
