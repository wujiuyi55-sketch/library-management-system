# 数据库设计

4 张表：`book`、`category`、`reader`、`borrow_record`。
MySQL 建表脚本：[`src/main/resources/db/schema-mysql.sql`](../src/main/resources/db/schema-mysql.sql)；
H2 演示脚本：[`schema-h2.sql`](../src/main/resources/db/schema-h2.sql)。

## 1. ER 关系

```
category 1 ──── n book 1 ──── n borrow_record n ──── 1 reader
```

- 一个分类下有多本书；一本书属于一个分类（可为空）
- 一条借阅记录关联一本书和一个读者
- 借阅记录的三个状态：`BORROWED`（在借）→ `OVERDUE`（超期未还）→ `RETURNED`（已归还）

## 2. 表结构

### 2.1 category 图书分类

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AI | 主键 |
| name | VARCHAR(50) | 分类名 |
| sort | INT | 排序号，升序展示 |

### 2.2 book 图书

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AI | 主键 |
| isbn | VARCHAR(20) | 唯一索引 `uk_book_isbn` |
| title | VARCHAR(200) | 书名，索引 `idx_book_title` |
| author | VARCHAR(100) | 作者 |
| publisher | VARCHAR(100) | 出版社 |
| category_id | BIGINT | 分类，索引 `idx_book_category` |
| total_stock | INT | 馆藏总量 |
| available_stock | INT | 可借数量，借书 -1、还书 +1 |
| status | INT | 1 可借阅，0 下架 |
| create_time / update_time | DATETIME | 创建、更新时间 |

### 2.3 reader 读者

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AI | 主键 |
| reader_no | VARCHAR(20) | 读者编号，唯一索引 `uk_reader_no` |
| name | VARCHAR(50) | 姓名 |
| phone | VARCHAR(20) | 手机号 |
| type | INT | 1 学生，2 教师 |
| status | INT | 1 正常，0 冻结 |
| max_borrow | INT | 最大在借数量（学生默认 5，教师 10） |
| create_time | DATETIME | 创建时间 |

### 2.4 borrow_record 借阅记录

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AI | 主键 |
| book_id | BIGINT | 图书 ID，索引 `idx_borrow_book(book_id, status)` |
| reader_id | BIGINT | 读者 ID，索引 `idx_borrow_reader(reader_id, status)` |
| borrow_time | DATETIME | 借出时间 |
| due_time | DATETIME | 应还时间 = 借出时间 + 30 天，索引 `idx_borrow_due` |
| return_time | DATETIME | 归还时间，未归还为 NULL |
| status | VARCHAR(16) | BORROWED / OVERDUE / RETURNED |
| overdue_days | INT | 超期天数，归还或扫描时写入 |
| fine | DECIMAL(10,2) | 罚金 = 超期天数 × 0.20 |
| create_time | DATETIME | 创建时间 |

> 索引都带上 `status`：业务查询几乎都会过滤状态，联合索引能直接命中
> 「某读者在借的还有几本」「某本书还没归还」这类高频查询。

## 3. 设计说明

### 3.1 为什么 `available_stock` 单独存一个字段？

如果只用 `total_stock - count(未归还)` 实时算，每次列表查询都要 join 借阅记录做聚合，数据量一大就慢。
单独存字段，读的时候是 O(1)；写的时候用带条件的 UPDATE 由数据库保证原子性：

```sql
UPDATE book SET available_stock = available_stock - 1
WHERE id = ? AND status = 1 AND available_stock > 0;
```

影响行数为 0 就代表库存不足，Service 层抛业务异常，事务回滚。

### 3.2 为什么不用外键约束？

- 删除图书 / 读者前，业务上要先校验有没有未归还记录，这个校验必须在 Service 层做（还要给出可读的提示）；
- 外键会限制批量导入、数据迁移和分表的灵活性；
- 代价是数据一致性要靠代码保证——本项目通过「删除前校验 + 事务」覆盖了主要场景。

### 3.3 为什么把超期天数、罚金落库？

借阅规则以后可能会改（比如罚金从 0.2 改成 0.5）。如果每次都实时算，历史记录会跟着规则一起变，对账就乱了。
归还时算一次写进记录，相当于快照。

### 3.4 状态用字符串而不是 0/1/2

日志、接口返回、数据库里都能直接看懂，排查问题时不用再查对照表。代价是占空间略大、写错不会有数据库报错，
所以状态值统一在 `BorrowRecord` 里定义成常量。
