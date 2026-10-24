package com.xunhai.library.service;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.domain.Book;
import com.xunhai.library.dto.BookQuery;

/**
 * 图书业务。
 */
public interface BookService {

    PageResult<Book> search(BookQuery query);

    Book get(Long id);

    Book create(Book book);

    Book update(Long id, Book book);

    void delete(Long id);
}
