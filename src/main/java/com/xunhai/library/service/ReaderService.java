package com.xunhai.library.service;

import com.xunhai.library.common.PageResult;
import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.ReaderQuery;

/**
 * 读者业务。
 */
public interface ReaderService {

    PageResult<Reader> search(ReaderQuery query);

    Reader get(Long id);

    Reader create(Reader reader);

    Reader update(Long id, Reader reader);

    void delete(Long id);
}
