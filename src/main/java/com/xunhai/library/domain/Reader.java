package com.xunhai.library.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 读者。
 */
@Data
public class Reader {

    private Long id;

    @NotBlank(message = "读者编号不能为空")
    @Size(max = 20, message = "读者编号长度不能超过 20")
    private String readerNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50")
    private String name;

    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /** 1 学生，2 教师 */
    @Min(value = 1, message = "读者类型只能是 1（学生）或 2（教师）")
    @Max(value = 2, message = "读者类型只能是 1（学生）或 2（教师）")
    private Integer type;

    /** 1 正常，0 冻结（冻结后不能借书） */
    private Integer status;

    /** 最大在借数量 */
    @Min(value = 1, message = "最大借阅数量至少为 1")
    @Max(value = 20, message = "最大借阅数量不能超过 20")
    private Integer maxBorrow;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
