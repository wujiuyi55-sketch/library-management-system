package com.xunhai.library.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 图书。
 */
@Data
public class Book {

    private Long id;

    @NotBlank(message = "ISBN 不能为空")
    @Size(max = 20, message = "ISBN 长度不能超过 20")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名长度不能超过 200")
    private String title;

    @Size(max = 100, message = "作者长度不能超过 100")
    private String author;

    @Size(max = 100, message = "出版社长度不能超过 100")
    private String publisher;

    private Long categoryId;

    @NotNull(message = "馆藏总量不能为空")
    @Min(value = 1, message = "馆藏总量至少为 1")
    private Integer totalStock;

    private Integer availableStock;

    /** 1 可借阅，0 下架 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 关联查询字段：分类名称 */
    private String categoryName;
}
