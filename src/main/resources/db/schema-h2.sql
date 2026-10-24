-- H2（MODE=MySQL）演示库结构，应用每次启动重建
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS reader;

CREATE TABLE category (
    id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    name   VARCHAR(50) NOT NULL,
    sort   INT         NOT NULL DEFAULT 0
);

CREATE TABLE book (
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
    update_time     DATETIME     NOT NULL
);
CREATE UNIQUE INDEX uk_book_isbn ON book (isbn);
CREATE INDEX idx_book_title ON book (title);
CREATE INDEX idx_book_category ON book (category_id);

CREATE TABLE reader (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    reader_no   VARCHAR(20) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    phone       VARCHAR(20),
    type        INT         NOT NULL DEFAULT 1,
    status      INT         NOT NULL DEFAULT 1,
    max_borrow  INT         NOT NULL DEFAULT 5,
    create_time DATETIME    NOT NULL
);
CREATE UNIQUE INDEX uk_reader_no ON reader (reader_no);

CREATE TABLE borrow_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id      BIGINT      NOT NULL,
    reader_id    BIGINT      NOT NULL,
    borrow_time  DATETIME    NOT NULL,
    due_time     DATETIME    NOT NULL,
    return_time  DATETIME,
    status       VARCHAR(16) NOT NULL,
    overdue_days INT         NOT NULL DEFAULT 0,
    fine         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    create_time  DATETIME    NOT NULL
);
CREATE INDEX idx_borrow_reader ON borrow_record (reader_id, status);
CREATE INDEX idx_borrow_book ON borrow_record (book_id, status);
CREATE INDEX idx_borrow_due ON borrow_record (due_time);
