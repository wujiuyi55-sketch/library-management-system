package com.xunhai.library.mapper;

import com.xunhai.library.domain.BorrowRecord;
import com.xunhai.library.dto.BorrowQuery;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借阅记录数据访问。
 */
@Mapper
public interface BorrowRecordMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO borrow_record (book_id, reader_id, borrow_time, due_time, return_time, status, overdue_days, fine, create_time) "
            + "VALUES (#{bookId}, #{readerId}, #{borrowTime}, #{dueTime}, NULL, #{status}, 0, 0.00, NOW())")
    int insert(BorrowRecord record);

    BorrowRecord findById(@Param("id") Long id);

    List<BorrowRecord> search(BorrowQuery query);

    long count(BorrowQuery query);

    /** 该读者当前未归还的记录数 */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId} AND status IN ('BORROWED', 'OVERDUE')")
    int countActiveByReader(@Param("readerId") Long readerId);

    /** 该读者是否已借这本书且未归还 */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE book_id = #{bookId} AND reader_id = #{readerId} "
            + "AND status IN ('BORROWED', 'OVERDUE')")
    int countActiveByBookAndReader(@Param("bookId") Long bookId, @Param("readerId") Long readerId);

    /** 该书是否有未归还记录（删除图书前校验） */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE book_id = #{bookId} AND status IN ('BORROWED', 'OVERDUE')")
    int countActiveByBook(@Param("bookId") Long bookId);

    /** 该读者是否有未归还记录（删除读者前校验） */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE reader_id = #{readerId} AND status IN ('BORROWED', 'OVERDUE')")
    int countActiveByReaderForDelete(@Param("readerId") Long readerId);

    /**
     * 归还：where 里带状态条件，重复提交时影响行数为 0，避免同一本书被归还两次。
     */
    @Update("UPDATE borrow_record SET return_time = #{returnTime}, status = 'RETURNED', "
            + "overdue_days = #{overdueDays}, fine = #{fine} "
            + "WHERE id = #{id} AND status IN ('BORROWED', 'OVERDUE')")
    int markReturned(@Param("id") Long id,
                     @Param("returnTime") LocalDateTime returnTime,
                     @Param("overdueDays") int overdueDays,
                     @Param("fine") BigDecimal fine);

    /** 未归还且已过应还时间的记录（定时任务用） */
    List<BorrowRecord> findDueBefore(@Param("now") LocalDateTime now);

    @Update("UPDATE borrow_record SET status = #{status}, overdue_days = #{overdueDays} WHERE id = #{id}")
    int updateOverdue(@Param("id") Long id, @Param("status") String status, @Param("overdueDays") int overdueDays);
}
