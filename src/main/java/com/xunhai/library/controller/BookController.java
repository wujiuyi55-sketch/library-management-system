package com.xunhai.library.controller;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.common.Result;
import com.xunhai.library.domain.Book;
import com.xunhai.library.dto.BookQuery;
import com.xunhai.library.service.BookService;
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
 * 图书检索与维护接口。
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /** 分页检索：/api/books?keyword=Java&categoryId=1&onlyAvailable=true&page=1&size=10 */
    @GetMapping
    public Result<PageResult<Book>> list(BookQuery query) {
        return Result.ok(bookService.search(query));
    }

    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Long id) {
        return Result.ok(bookService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Book> create(@Valid @RequestBody Book book) {
        return Result.ok(bookService.create(book));
    }

    @PutMapping("/{id}")
    public Result<Book> update(@PathVariable Long id, @Valid @RequestBody Book book) {
        return Result.ok(bookService.update(id, book));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.ok();
    }
}
