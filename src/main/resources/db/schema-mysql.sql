-- MySQL 8.0 生产库结构（首次部署执行一次）
-- mysql -uroot -p library < db/schema-mysql.sql

CREATE TABLE IF NOT EXISTS category (
    id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    name   VARCHAR(50) NOT NULL,
    sort   INT         NOT NULL DEFAULT 0
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书分类';

CREATE TABLE IF NOT EXISTS book (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn            VARCHAR(20)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    author          VARCHAR(100),
    publisher       VARCHAR(100),
    category_id     BIGINT,
    total_stock     INT          NOT NULL DEFAULT 0,
    available_stock INT          NOT NULL DEFAULT 0,
    status          INT          NOT NULL DEFAULT 1,
    create_time     DATETIME     NOT NULL,
    update_time     DATETIME     NOT NULL,
    UNIQUE KEY uk_book_isbn (isbn),
    KEY idx_book_title (title),
    KEY idx_book_category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '图书';

CREATE TABLE IF NOT EXISTS reader (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_no   VARCHAR(20) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    phone       VARCHAR(20),
    type        INT         NOT NULL DEFAULT 1,
    status      INT         NOT NULL DEFAULT 1,
    max_borrow  INT         NOT NULL DEFAULT 5,
    create_time DATETIME    NOT NULL,
    UNIQUE KEY uk_reader_no (reader_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '读者';

CREATE TABLE IF NOT EXISTS borrow_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id      BIGINT         NOT NULL,
    reader_id    BIGINT         NOT NULL,
    borrow_time  DATETIME       NOT NULL,
    due_time     DATETIME       NOT NULL,
    return_time  DATETIME,
    status       VARCHAR(16)    NOT NULL,
    overdue_days INT            NOT NULL DEFAULT 0,
    fine         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    create_time  DATETIME       NOT NULL,
    KEY idx_borrow_reader (reader_id, status),
    KEY idx_borrow_book (book_id, status),
    KEY idx_borrow_due (due_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '借阅记录';
