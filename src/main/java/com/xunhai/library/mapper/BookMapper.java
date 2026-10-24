package com.xunhai.library.mapper;

import com.xunhai.library.domain.Book;
import com.xunhai.library.dto.BookQuery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 图书数据访问。简单 CRUD 用注解，动态检索与联表查询放在 BookMapper.xml。
 */
@Mapper
public interface BookMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO book (isbn, title, author, publisher, category_id, total_stock, available_stock, status, create_time, update_time) "
            + "VALUES (#{isbn}, #{title}, #{author}, #{publisher}, #{categoryId}, #{totalStock}, #{availableStock}, #{status}, NOW(), NOW())")
    int insert(Book book);

    @Update("UPDATE book SET isbn = #{isbn}, title = #{title}, author = #{author}, publisher = #{publisher}, "
            + "category_id = #{categoryId}, total_stock = #{totalStock}, available_stock = #{availableStock}, "
            + "status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(Book book);

    @Delete("DELETE FROM book WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    Book findById(@Param("id") Long id);

    List<Book> search(BookQuery query);

    long count(BookQuery query);

    @Select("<script>SELECT COUNT(*) FROM book WHERE isbn = #{isbn}"
            + "<if test='excludeId != null'> AND id &lt;&gt; #{excludeId}</if></script>")
    int countByIsbn(@Param("isbn") String isbn, @Param("excludeId") Long excludeId);

    /**
     * 扣减可借库存：条件里带 available_stock > 0，靠数据库行锁保证并发下不超卖，
     * 返回 0 表示库存已被别人抢完。
     */
    @Update("UPDATE book SET available_stock = available_stock - 1, update_time = NOW() "
            + "WHERE id = #{id} AND status = 1 AND available_stock > 0")
    int decreaseStock(@Param("id") Long id);

    /**
     * 归还：库存加一，同时不能超过馆藏总量。
     */
    @Update("UPDATE book SET available_stock = available_stock + 1, update_time = NOW() "
            + "WHERE id = #{id} AND available_stock < total_stock")
    int increaseStock(@Param("id") Long id);
}
