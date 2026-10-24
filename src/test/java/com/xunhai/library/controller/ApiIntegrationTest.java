package com.xunhai.library.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口层测试：验证统一响应体、全局异常处理、参数校验在 HTTP 层真的生效。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("图书检索：返回统一响应体与分页结构")
    void searchBooksShouldReturnPageResult() throws Exception {
        mockMvc.perform(get("/api/books").param("keyword", "Java").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.list[0].title").value(containsString("Java")));
    }

    @Test
    @DisplayName("借书：返回 201 与在借记录")
    void borrowShouldReturnCreatedRecord() throws Exception {
        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":2,\"readerId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("BORROWED"))
                .andExpect(jsonPath("$.data.bookId").value(2))
                .andExpect(jsonPath("$.data.dueTime").isNotEmpty());
    }

    @Test
    @DisplayName("重复借阅：返回 400 与可读的业务提示")
    void borrowTwiceShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/borrows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"readerId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("尚未归还")));
    }

    @Test
    @DisplayName("参数校验：书名为空时返回 400 与字段提示")
    void createBookWithoutTitleShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":\"9787000000002\",\"title\":\"\",\"totalStock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("书名不能为空")));
    }

    @Test
    @DisplayName("还书：第一次成功，第二次返回 400")
    void returnTwiceShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/borrows/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"));

        mockMvc.perform(post("/api/borrows/1/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("已归还")));
    }

    @Test
    @DisplayName("统计概览：字段齐全")
    void overviewShouldReturnAllFields() throws Exception {
        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.availableStock").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.readerCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.borrowingCount").isNumber())
                .andExpect(jsonPath("$.data.overdueCount").isNumber());
    }
}
