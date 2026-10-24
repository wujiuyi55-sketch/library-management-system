package com.xunhai.library.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 借书请求。
 */
@Data
public class BorrowRequest {

    @NotNull(message = "图书 ID 不能为空")
    private Long bookId;

    @NotNull(message = "读者 ID 不能为空")
    private Long readerId;
}
