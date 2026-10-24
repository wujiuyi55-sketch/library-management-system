package com.xunhai.library.mapper;

import com.xunhai.library.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图书分类数据访问。
 */
@Mapper
public interface CategoryMapper {

    @Select("SELECT id, name, sort FROM category ORDER BY sort ASC, id ASC")
    List<Category> findAll();
}
