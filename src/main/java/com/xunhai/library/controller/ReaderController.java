package com.xunhai.library.controller;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.common.Result;
import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.ReaderQuery;
import com.xunhai.library.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 读者管理与检索接口。
 */
@RestController
@RequestMapping("/api/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final ReaderService readerService;

    @GetMapping
    public Result<PageResult<Reader>> list(ReaderQuery query) {
        return Result.ok(readerService.search(query));
    }

    @GetMapping("/{id}")
    public Result<Reader> detail(@PathVariable Long id) {
        return Result.ok(readerService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Reader> create(@Valid @RequestBody Reader reader) {
        return Result.ok(readerService.create(reader));
    }

    @PutMapping("/{id}")
    public Result<Reader> update(@PathVariable Long id, @Valid @RequestBody Reader reader) {
        return Result.ok(readerService.update(id, reader));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        readerService.delete(id);
        return Result.ok();
    }
}
