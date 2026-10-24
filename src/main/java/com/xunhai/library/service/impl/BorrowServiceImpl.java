package com.xunhai.library.service.impl;

import com.xunhai.library.common.BusinessException;
import com.xunhai.library.common.PageResult;
import com.xunhai.library.config.LibraryProperties;
import com.xunhai.library.domain.Book;
import com.xunhai.library.domain.BorrowRecord;
import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.BorrowQuery;
import com.xunhai.library.dto.BorrowRequest;
import com.xunhai.library.mapper.BookMapper;
import com.xunhai.library.mapper.BorrowRecordMapper;
import com.xunhai.library.mapper.ReaderMapper;
import com.xunhai.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 借还书业务实现。
 *
 * <p>核心是三条校验：读者状态与借阅上限、图书是否可借、库存扣减必须原子。
 * 库存扣减用「带条件的 UPDATE」，由数据库行锁保证并发下不会超卖。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BookMapper bookMapper;
    private final ReaderMapper readerMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final LibraryProperties properties;

    @Override
    @Transactional
    public BorrowRecord borrow(BorrowRequest request) {
        Reader reader = readerMapper.findById(request.getReaderId());
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        if (reader.getStatus() == null || reader.getStatus() != 1) {
            throw new BusinessException("该读者已被冻结，无法借书");
        }

        Book book = bookMapper.findById(request.getBookId());
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        if (book.getStatus() == null || book.getStatus() != 1) {
            throw new BusinessException("该图书已下架，无法借阅");
        }

        int active = borrowRecordMapper.countActiveByReader(reader.getId());
        int maxBorrow = reader.getMaxBorrow() == null ? 5 : reader.getMaxBorrow();
        if (active >= maxBorrow) {
            throw new BusinessException("已达到最大借阅数量（" + maxBorrow + " 本），请先归还");
        }
        if (borrowRecordMapper.countActiveByBookAndReader(book.getId(), reader.getId()) > 0) {
            throw new BusinessException("该读者已借阅此书且尚未归还");
        }

        // 条件更新：available_stock > 0 才扣减，影响行数为 0 说明被别人抢完
        if (bookMapper.decreaseStock(book.getId()) == 0) {
            throw new BusinessException("该图书已无可借库存");
        }

        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setBookId(book.getId());
        record.setReaderId(reader.getId());
        record.setBorrowTime(now);
        record.setDueTime(now.plusDays(properties.getBorrowDays()));
        record.setStatus(BorrowRecord.STATUS_BORROWED);
        borrowRecordMapper.insert(record);

        log.info("借书成功: recordId={}, bookId={}, readerId={}", record.getId(), book.getId(), reader.getId());
        return borrowRecordMapper.findById(record.getId());
    }

    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        if (BorrowRecord.STATUS_RETURNED.equals(record.getStatus())) {
            throw new BusinessException("该记录已归还，请勿重复操作");
        }

        LocalDateTime now = LocalDateTime.now();
        long overdueDays = Math.max(0, ChronoUnit.DAYS.between(record.getDueTime(), now));
        BigDecimal fine = properties.getFinePerDay()
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);

        // 带状态条件，重复提交时影响行数为 0
        if (borrowRecordMapper.markReturned(recordId, now, (int) overdueDays, fine) == 0) {
            throw new BusinessException("该记录已归还，请勿重复操作");
        }
        bookMapper.increaseStock(record.getBookId());

        log.info("还书成功: recordId={}, overdueDays={}, fine={}", recordId, overdueDays, fine);
        return borrowRecordMapper.findById(recordId);
    }

    @Override
    public PageResult<BorrowRecord> search(BorrowQuery query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        size = Math.min(size, properties.getMaxPageSize());
        query.setPage(page);
        query.setSize(size);

        long total = borrowRecordMapper.count(query);
        List<BorrowRecord> list = total == 0 ? List.of() : borrowRecordMapper.search(query);
        return PageResult.of(total, page, size, list);
    }

    @Override
    @Transactional
    public int refreshOverdue() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> due = borrowRecordMapper.findDueBefore(now);
        int updated = 0;
        for (BorrowRecord record : due) {
            long days = Math.max(0, ChronoUnit.DAYS.between(record.getDueTime(), now));
            if (days <= 0) {
                continue;
            }
            borrowRecordMapper.updateOverdue(record.getId(), BorrowRecord.STATUS_OVERDUE, (int) days);
            updated++;
        }
        if (updated > 0) {
            log.info("刷新超期状态完成，共 {} 条", updated);
        }
        return updated;
    }
}
