package com.xunhai.library.task;

import com.xunhai.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超期扫描：每天凌晨 1 点把过了应还时间仍未归还的记录标成 OVERDUE，并刷新超期天数。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueTask {

    private final BorrowService borrowService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshOverdue() {
        int updated = borrowService.refreshOverdue();
        log.info("超期扫描结束，更新 {} 条", updated);
    }
}
