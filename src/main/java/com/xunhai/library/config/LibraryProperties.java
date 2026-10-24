package com.xunhai.library.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 借阅规则配置（application.yml 中的 library.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "library")
public class LibraryProperties {

    /** 借期（天） */
    private int borrowDays = 30;

    /** 超期罚金（元/天） */
    private BigDecimal finePerDay = new BigDecimal("0.20");

    /** 单页最大条数 */
    private int maxPageSize = 100;
}
