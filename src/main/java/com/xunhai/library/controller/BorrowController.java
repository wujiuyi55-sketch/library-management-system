package com.xunhai.library.controller;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.common.Result;
import com.xunhai.library.domain.BorrowRecord;
import com.xunhai.library.dto.BorrowQuery;
import com.xunhai.library.dto.BorrowRequest;
import com.xunhai.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 借书、还书、借阅记录。
 */
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /** 借书 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BorrowRecord> borrow(@Valid @RequestBody BorrowRequest request) {
        return Result.ok(borrowService.borrow(request));
    }

    /** 还书 */
    @PostMapping("/{id}/return")
    public Result<BorrowRecord> returnBook(@PathVariable Long id) {
        return Result.ok(borrowService.returnBook(id));
    }

    /** 借阅记录分页：/api/borrows?status=BORROWED&readerId=1&page=1&size=10 */
    @GetMapping
    public Result<PageResult<BorrowRecord>> list(BorrowQuery query) {
        return Result.ok(borrowService.search(query));
    }

    /** 手动刷新超期状态（定时任务每天 01:00 也会跑一次） */
    @PostMapping("/refresh-overdue")
    public Result<Integer> refreshOverdue() {
        return Result.ok(borrowService.refreshOverdue());
    }
}
