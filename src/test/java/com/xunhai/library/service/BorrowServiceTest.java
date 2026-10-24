package com.xunhai.library.service;

import com.xunhai.library.common.BusinessException;
import com.xunhai.library.domain.Book;
import com.xunhai.library.domain.BorrowRecord;
import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.BorrowRequest;
import com.xunhai.library.mapper.BookMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 借还书核心逻辑测试（H2 内存库，跑在事务里，结束自动回滚）。
 */
@SpringBootTest
@Transactional
class BorrowServiceTest {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private ReaderService readerService;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private com.xunhai.library.mapper.BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Reader createReader(String readerNo, int maxBorrow, int status) {
        Reader reader = new Reader();
        reader.setReaderNo(readerNo);
        reader.setName("测试读者");
        reader.setPhone("13000000000");
        reader.setType(1);
        reader.setStatus(status);
        reader.setMaxBorrow(maxBorrow);
        return readerService.create(reader);
    }

    private BorrowRequest request(Long bookId, Long readerId) {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(bookId);
        request.setReaderId(readerId);
        return request;
    }

    @Test
    @DisplayName("借书成功：库存减一，生成 30 天借期的在借记录")
    void borrowShouldDecreaseAvailableStock() {
        Reader reader = createReader("T-BORROW-01", 5, 1);
        Book before = bookMapper.findById(2L);

        BorrowRecord record = borrowService.borrow(request(2L, reader.getId()));

        assertThat(record.getId()).isNotNull();
        assertThat(record.getStatus()).isEqualTo(BorrowRecord.STATUS_BORROWED);
        assertThat(record.getDueTime()).isAfter(LocalDateTime.now().plusDays(29));
        assertThat(bookMapper.findById(2L).getAvailableStock()).isEqualTo(before.getAvailableStock() - 1);
    }

    @Test
    @DisplayName("库存为 0 时借书失败，库存不会被扣成负数")
    void borrowShouldFailWhenNoStock() {
        Reader reader = createReader("T-BORROW-02", 5, 1);
        // 7 号书共 2 册，先借空
        bookMapper.decreaseStock(7L);
        bookMapper.decreaseStock(7L);
        assertThat(bookMapper.findById(7L).getAvailableStock()).isZero();

        assertThatThrownBy(() -> borrowService.borrow(request(7L, reader.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存");
        assertThat(bookMapper.findById(7L).getAvailableStock()).isZero();
    }

    @Test
    @DisplayName("读者被冻结时不能借书")
    void borrowShouldFailWhenReaderFrozen() {
        Reader reader = createReader("T-BORROW-03", 5, 0);

        assertThatThrownBy(() -> borrowService.borrow(request(2L, reader.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("冻结");
    }

    @Test
    @DisplayName("同一本书未归还时不能重复借阅")
    void borrowShouldFailWhenAlreadyBorrowed() {
        // 1 号读者已借 1 号书且未归还
        assertThatThrownBy(() -> borrowService.borrow(request(1L, 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("尚未归还");
    }

    @Test
    @DisplayName("达到最大借阅数量时不能再借")
    void borrowShouldFailWhenReachMaxBorrow() {
        Reader reader = createReader("T-BORROW-04", 1, 1);
        borrowService.borrow(request(2L, reader.getId()));

        assertThatThrownBy(() -> borrowService.borrow(request(4L, reader.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最大借阅数量");
    }

    @Test
    @DisplayName("按时归还：库存恢复，罚金为 0")
    void returnShouldRestoreStockWithoutFine() {
        Reader reader = createReader("T-RETURN-01", 5, 1);
        BorrowRecord record = borrowService.borrow(request(2L, reader.getId()));
        int afterBorrow = bookMapper.findById(2L).getAvailableStock();

        BorrowRecord returned = borrowService.returnBook(record.getId());

        assertThat(returned.getStatus()).isEqualTo(BorrowRecord.STATUS_RETURNED);
        assertThat(returned.getReturnTime()).isNotNull();
        assertThat(returned.getOverdueDays()).isZero();
        assertThat(returned.getFine()).isEqualByComparingTo("0.00");
        assertThat(bookMapper.findById(2L).getAvailableStock()).isEqualTo(afterBorrow + 1);
    }

    @Test
    @DisplayName("超期归还：按天计算超期天数与罚金")
    void returnOverdueShouldChargeFine() {
        Reader reader = createReader("T-RETURN-02", 5, 1);
        // 直接构造一条 5 天前就该归还的记录，模拟超期
        BorrowRecord record = new BorrowRecord();
        record.setBookId(2L);
        record.setReaderId(reader.getId());
        record.setBorrowTime(LocalDateTime.now().minusDays(35));
        record.setDueTime(LocalDateTime.now().minusDays(5));
        record.setStatus(BorrowRecord.STATUS_BORROWED);
        borrowRecordMapper.insert(record);

        BorrowRecord returned = borrowService.returnBook(record.getId());

        assertThat(returned.getStatus()).isEqualTo(BorrowRecord.STATUS_RETURNED);
        assertThat(returned.getOverdueDays()).isGreaterThanOrEqualTo(5);
        // 0.20 元/天
        assertThat(returned.getFine()).isGreaterThanOrEqualTo(new java.math.BigDecimal("1.00"));
    }

    @Test
    @DisplayName("重复归还同一记录会失败，库存不会被加两次")
    void returnTwiceShouldFail() {
        Reader reader = createReader("T-RETURN-03", 5, 1);
        BorrowRecord record = borrowService.borrow(request(2L, reader.getId()));
        borrowService.returnBook(record.getId());
        int stockAfterFirstReturn = bookMapper.findById(2L).getAvailableStock();

        assertThatThrownBy(() -> borrowService.returnBook(record.getId()))
                .isInstanceOf(BusinessException.class);
        assertThat(bookMapper.findById(2L).getAvailableStock()).isEqualTo(stockAfterFirstReturn);
    }

    @Test
    @DisplayName("刷新超期状态：把过期的在借记录标成 OVERDUE")
    void refreshOverdueShouldMarkExpiredRecords() {
        Reader reader = createReader("T-OVERDUE-01", 5, 1);
        BorrowRecord record = borrowService.borrow(request(2L, reader.getId()));
        jdbcTemplate.update("UPDATE borrow_record SET due_time = ? WHERE id = ?",
                LocalDateTime.now().minusDays(3), record.getId());

        int updated = borrowService.refreshOverdue();

        assertThat(updated).isGreaterThanOrEqualTo(1);
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM borrow_record WHERE id = ?", String.class, record.getId());
        assertThat(status).isEqualTo(BorrowRecord.STATUS_OVERDUE);
    }
}
