package com.xunhai.library.service.impl;

import com.xunhai.library.common.BusinessException;
import com.xunhai.library.common.PageResult;
import com.xunhai.library.config.LibraryProperties;
import com.xunhai.library.domain.Book;
import com.xunhai.library.dto.BookQuery;
import com.xunhai.library.mapper.BookMapper;
import com.xunhai.library.mapper.BorrowRecordMapper;
import com.xunhai.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书业务实现。
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final int STATUS_ON_SHELF = 1;

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final LibraryProperties properties;

    @Override
    public PageResult<Book> search(BookQuery query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        size = Math.min(size, properties.getMaxPageSize());
        query.setPage(page);
        query.setSize(size);

        long total = bookMapper.count(query);
        List<Book> list = total == 0 ? List.of() : bookMapper.search(query);
        return PageResult.of(total, page, size, list);
    }

    @Override
    public Book get(Long id) {
        Book book = bookMapper.findById(id);
        if (book == null) {
            throw new BusinessException(404, "图书不存在");
        }
        return book;
    }

    @Override
    @Transactional
    public Book create(Book book) {
        if (bookMapper.countByIsbn(book.getIsbn(), null) > 0) {
            throw new BusinessException("ISBN 已存在：" + book.getIsbn());
        }
        book.setStatus(book.getStatus() == null ? STATUS_ON_SHELF : book.getStatus());
        // 新书全部可借
        book.setAvailableStock(book.getTotalStock());
        bookMapper.insert(book);
        return bookMapper.findById(book.getId());
    }

    @Override
    @Transactional
    public Book update(Long id, Book book) {
        Book exist = get(id);
        if (!exist.getIsbn().equals(book.getIsbn()) && bookMapper.countByIsbn(book.getIsbn(), id) > 0) {
            throw new BusinessException("ISBN 已存在：" + book.getIsbn());
        }
        // 在借册数 = 馆藏总量 - 可借库存，调整馆藏时不能把在借的书「调没了」
        int borrowed = exist.getTotalStock() - exist.getAvailableStock();
        if (book.getTotalStock() < borrowed) {
            throw new BusinessException("馆藏总量不能小于当前在借册数（" + borrowed + " 册）");
        }
        book.setId(id);
        book.setStatus(book.getStatus() == null ? exist.getStatus() : book.getStatus());
        book.setAvailableStock(book.getTotalStock() - borrowed);
        bookMapper.update(book);
        return bookMapper.findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        get(id);
        if (borrowRecordMapper.countActiveByBook(id) > 0) {
            throw new BusinessException("该图书还有未归还的借阅记录，不能删除");
        }
        bookMapper.deleteById(id);
    }
}
