package com.xunhai.library.mapper;

import com.xunhai.library.vo.CategoryStatVO;
import com.xunhai.library.vo.HotBookVO;
import com.xunhai.library.vo.MonthlyStatVO;
import com.xunhai.library.vo.OverdueVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计查询（聚合、联表，全部放在 StatsMapper.xml）。
 */
@Mapper
public interface StatsMapper {

    long countBooks();

    long sumAvailableStock();

    long countReaders();

    long countBorrowing();

    long countOverdue(@Param("now") LocalDateTime now);

    List<HotBookVO> topBooks(@Param("limit") int limit);

    List<CategoryStatVO> categoryStats();

    List<MonthlyStatVO> monthlyStats(@Param("start") LocalDateTime start);

    List<OverdueVO> overdueList(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
