package com.xunhai.library.mapper;

import com.xunhai.library.domain.Reader;
import com.xunhai.library.dto.ReaderQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 读者数据访问。
 */
@Mapper
public interface ReaderMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO reader (reader_no, name, phone, type, status, max_borrow, create_time) "
            + "VALUES (#{readerNo}, #{name}, #{phone}, #{type}, #{status}, #{maxBorrow}, NOW())")
    int insert(Reader reader);

    @Update("UPDATE reader SET reader_no = #{readerNo}, name = #{name}, phone = #{phone}, "
            + "type = #{type}, status = #{status}, max_borrow = #{maxBorrow} WHERE id = #{id}")
    int update(Reader reader);

    @Delete("DELETE FROM reader WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM reader WHERE id = #{id}")
    Reader findById(@Param("id") Long id);

    @Select("SELECT * FROM reader WHERE reader_no = #{readerNo}")
    Reader findByReaderNo(@Param("readerNo") String readerNo);

    @Select("<script>SELECT COUNT(*) FROM reader WHERE reader_no = #{readerNo}"
            + "<if test='excludeId != null'> AND id &lt;&gt; #{excludeId}</if></script>")
    int countByReaderNo(@Param("readerNo") String readerNo, @Param("excludeId") Long excludeId);

    List<Reader> search(ReaderQuery query);

    long count(ReaderQuery query);
}
