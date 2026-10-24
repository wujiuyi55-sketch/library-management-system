package com.xunhai.library.controller;

import com.xunhai.library.common.Result;
import com.xunhai.library.domain.Category;
import com.xunhai.library.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书分类（下拉框数据）。
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @GetMapping
    public Result<List<Category>> list() {
        return Result.ok(categoryMapper.findAll());
    }
}
