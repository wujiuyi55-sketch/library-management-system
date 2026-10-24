package com.xunhai.library.service;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.domain.BorrowRecord;
import com.xunhai.library.dto.BorrowQuery;
import com.xunhai.library.dto.BorrowRequest;

/**
 * 借还书业务。
 */
public interface BorrowService {

    /** 借书：校验读者、库存、借阅上限、重复借阅，成功后库存减一 */
    BorrowRecord borrow(BorrowRequest request);

    /** 还书：回填归还时间、计算超期天数与罚金，成功后库存加一 */
    BorrowRecord returnBook(Long recordId);

    PageResult<BorrowRecord> search(BorrowQuery query);

    /** 刷新超期状态，返回本次更新的记录数 */
    int refreshOverdue();
}
