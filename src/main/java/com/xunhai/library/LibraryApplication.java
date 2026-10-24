package com.xunhai.library;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 图书管理系统启动类。
 *
 * <p>技术栈：Spring Boot 2.7 + MyBatis + MySQL（演示环境使用 H2 的 MySQL 兼容模式）。</p>
 */
@EnableScheduling
@MapperScan("com.xunhai.library.mapper")
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
