package com.xunhai.library.service.impl;

import com.xunhai.library.common.BusinessException;
import com.xunhai.library.common.PageResult;
import com.xunhai.library.config.LibraryProperties;
import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.ReaderQuery;
import com.xunhai.library.mapper.BorrowRecordMapper;
import com.xunhai.library.mapper.ReaderMapper;
import com.xunhai.library.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 读者业务实现。
 */
@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {

    private static final int STATUS_NORMAL = 1;

    private final ReaderMapper readerMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final LibraryProperties properties;

    @Override
    public PageResult<Reader> search(ReaderQuery query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        size = Math.min(size, properties.getMaxPageSize());
        query.setPage(page);
        query.setSize(size);

        long total = readerMapper.count(query);
        List<Reader> list = total == 0 ? List.of() : readerMapper.search(query);
        return PageResult.of(total, page, size, list);
    }

    @Override
    public Reader get(Long id) {
        Reader reader = readerMapper.findById(id);
        if (reader == null) {
            throw new BusinessException(404, "读者不存在");
        }
        return reader;
    }

    @Override
    @Transactional
    public Reader create(Reader reader) {
        if (readerMapper.countByReaderNo(reader.getReaderNo(), null) > 0) {
            throw new BusinessException("读者编号已存在：" + reader.getReaderNo());
        }
        reader.setStatus(reader.getStatus() == null ? STATUS_NORMAL : reader.getStatus());
        reader.setType(reader.getType() == null ? 1 : reader.getType());
        reader.setMaxBorrow(reader.getMaxBorrow() == null ? 5 : reader.getMaxBorrow());
        readerMapper.insert(reader);
        return readerMapper.findById(reader.getId());
    }

    @Override
    @Transactional
    public Reader update(Long id, Reader reader) {
        get(id);
        if (readerMapper.countByReaderNo(reader.getReaderNo(), id) > 0) {
            throw new BusinessException("读者编号已存在：" + reader.getReaderNo());
        }
        reader.setId(id);
        reader.setStatus(reader.getStatus() == null ? STATUS_NORMAL : reader.getStatus());
        reader.setType(reader.getType() == null ? 1 : reader.getType());
        reader.setMaxBorrow(reader.getMaxBorrow() == null ? 5 : reader.getMaxBorrow());
        readerMapper.update(reader);
        return readerMapper.findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        get(id);
        if (borrowRecordMapper.countActiveByReaderForDelete(id) > 0) {
            throw new BusinessException("该读者还有未归还的图书，不能删除");
        }
        readerMapper.deleteById(id);
    }
}
