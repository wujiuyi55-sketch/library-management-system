# 图书管理系统 Library Management System

一套拿来就能跑的图书借阅管理系统：**Spring Boot 2.7 + MyBatis + MySQL 8**，包含图书检索、读者管理、借还书、超期与借阅统计，
自带一个不需要前端工程的演示页面，克隆下来 `mvn spring-boot:run` 就能用。

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)
![MyBatis](https://img.shields.io/badge/MyBatis-2.3.1-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1)
![License](https://img.shields.io/badge/License-MIT-green)

> 演示环境默认使用 H2（MySQL 兼容模式），**不需要装数据库**；生产环境切换 `mysql` profile 即可。

---

## 功能

| 模块 | 能力 |
| --- | --- |
| 图书管理 | 新增 / 修改 / 删除 / 详情；按书名、作者、ISBN 模糊检索；分类筛选；只看有货；分页 |
| 读者管理 | 新增 / 修改 / 删除；读者编号唯一校验；借阅上限；冻结状态（冻结后不能借书） |
| 借书 | 校验读者状态、图书状态、库存、借阅上限、重复借阅；库存**原子扣减**；自动生成 30 天借期 |
| 还书 | 回填归还时间；按当前时间计算超期天数与罚金（0.20 元/天）；库存回滚；防重复归还 |
| 统计 | 概览（种类 / 可借 / 读者 / 在借 / 超期）、热门图书 TOP N、分类分布、近 N 月借阅量、超期明细 |
| 工程化 | 统一响应体、全局异常处理、参数校验、事务、定时任务、JUnit 单测、分页封装 |

演示页面在 `http://localhost:8080/`，三个页签：**图书检索 / 借阅记录 / 数据看板**（支持 `#records`、`#stats` 直接跳转）。

## 技术栈

- **Java 17** / Spring Boot 2.7.18
- **MyBatis 2.3.1**：简单 CRUD 用注解，动态检索与联表统计写在 XML
- **MySQL 8.0**（生产）/ **H2**（演示、单元测试）
- Maven、Lombok、JUnit 5 + AssertJ
- 原生 HTML + CSS + JavaScript 演示页（不引入前端框架，零构建）

## 快速开始

前置：JDK 17+、Maven 3.8+。

```bash
git clone https://github.com/wujiuyi55-sketch/library-management-system.git
cd library-management-system
mvn spring-boot:run
```

打开 <http://localhost:8080/> 即可。启动时会自动建表并写入演示数据（H2 内存库，重启即重置）。

演示读者编号：`R2026001` 张三、`R2026002` 李四、`R2026003` 王五（有一本超期）、`T2026001` 赵敏（教师，可借 10 本）。

### 切换到 MySQL

```bash
# 1. 建库
mysql -uroot -p -e "CREATE DATABASE library DEFAULT CHARACTER SET utf8mb4;"

# 2. 初始化表结构与演示数据（只需执行一次）
mysql -uroot -p library < src/main/resources/db/schema-mysql.sql
mysql -uroot -p library < src/main/resources/db/data.sql

# 3. 用环境变量传连接信息，不要写进配置文件
export DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=library DB_USER=root DB_PASSWORD=你的密码

# 4. 启动
mvn package -DskipTests
java -jar target/library-management-system.jar --spring.profiles.active=mysql
```

> Windows PowerShell 用 `$env:DB_PASSWORD="你的密码"` 设置环境变量。

## 接口一览

完整参数与示例见 [docs/api.md](docs/api.md)。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/books` | 图书分页检索（keyword / categoryId / onlyAvailable / page / size） |
| GET | `/api/books/{id}` | 图书详情 |
| POST | `/api/books` | 新增图书 |
| PUT | `/api/books/{id}` | 修改图书（调整馆藏时会校验在借册数） |
| DELETE | `/api/books/{id}` | 删除图书（有未归还记录时拒绝） |
| GET | `/api/categories` | 分类列表 |
| GET | `/api/readers` | 读者分页检索 |
| POST/PUT/DELETE | `/api/readers...` | 读者维护 |
| POST | `/api/borrows` | 借书 `{"bookId":1,"readerId":1}` |
| POST | `/api/borrows/{id}/return` | 还书 |
| GET | `/api/borrows` | 借阅记录（status / readerId / bookId / page / size） |
| POST | `/api/borrows/refresh-overdue` | 手动刷新超期状态 |
| GET | `/api/stats/overview` | 概览 |
| GET | `/api/stats/hot-books` | 热门图书 |
| GET | `/api/stats/categories` | 分类借阅分布 |
| GET | `/api/stats/monthly` | 近 N 月借阅量 |
| GET | `/api/stats/overdue` | 超期未还明细 |

统一响应格式：

```json
{ "code": 0, "message": "success", "data": {} }
```

`code = 0` 成功；业务错误返回 HTTP 400 + `code = 400`，系统异常返回 HTTP 500 + `code = 500`。

## 数据库设计

4 张表：`book`、`category`、`reader`、`borrow_record`，详见 [docs/database.md](docs/database.md)。

设计上做了几个取舍：

1. **`available_stock` 单独存字段**，不用「总数 - 在借数」实时算：借还书高频，避免每次查询都做聚合；
   扣减时用 `WHERE available_stock > 0` 让数据库行锁兜底，天然防超卖。
2. **不建外键**，靠 Service 层校验（删除前查未归还记录），避免升级、批量导入、分库分表时的额外约束开销。
3. **超期天数、罚金落库**：归还时算一次并存下来，历史账目不会因为以后再改规则而变化。
4. 借阅状态用字符串 `BORROWED / OVERDUE / RETURNED`，比数字状态码可读，日志里一眼能看懂。

## 关键实现

**1. 借书不超卖：带条件的 UPDATE**

```java
@Update("UPDATE book SET available_stock = available_stock - 1, update_time = NOW() "
        + "WHERE id = #{id} AND status = 1 AND available_stock > 0")
int decreaseStock(@Param("id") Long id);
```

影响行数为 0 说明库存已经被别人抢完，直接抛出 `BusinessException("该图书已无可借库存")`，
整个方法在 `@Transactional` 里，异常会回滚，不会出现「扣了库存没生成记录」。

**2. 借书五道校验**（`BorrowServiceImpl#borrow`）

读者存在 → 读者未冻结 → 图书在架 → 未超过借阅上限 → 未重复借同一本书 → 扣库存 → 生成记录。

**3. 还书幂等**

```java
@Update("UPDATE borrow_record SET return_time = #{returnTime}, status = 'RETURNED', "
        + "overdue_days = #{overdueDays}, fine = #{fine} "
        + "WHERE id = #{id} AND status IN ('BORROWED', 'OVERDUE')")
```

前端重复点「还书」时，第二次影响行数为 0，直接返回业务错误，库存不会被加两次。

**4. 超期天数在应用层算**

```java
long overdueDays = Math.max(0, ChronoUnit.DAYS.between(record.getDueTime(), LocalDateTime.now()));
```

不用 `DATEDIFF` / `TIMESTAMPDIFF`，因为这些函数在 MySQL 和 H2 里参数不一致；放在 Java 里只依赖 JDK，
顺便让「罚金规则」集中在 `LibraryProperties`（借期 30 天、0.2 元/天，写在 `application.yml`）。

**5. 统计：SQL 聚合 + 应用层补齐**

月度借阅量用 `EXTRACT(YEAR/MONTH ...)` 分组（标准 SQL，MySQL 与 H2 都能跑），
Service 层再把没有借阅记录的月份补 0，前端画图 X 轴才连续。

## 测试

```bash
mvn test
```

9 个用例覆盖核心链路（H2 内存库，测试事务自动回滚）：

- 借书成功：库存减一、生成 30 天借期记录
- 库存为 0 时借书失败，库存不会被扣成负数
- 读者冻结、达到借阅上限、重复借阅分别拦截
- 按时归还：库存恢复、罚金 0
- 超期归还：按天计算超期天数与罚金
- 重复归还失败，库存不会被加两次
- 超期扫描任务把过期记录标记为 `OVERDUE`

## 项目结构

```
src/main/java/com/xunhai/library
├── LibraryApplication.java        启动类
├── common/                        统一响应 Result、分页 PageResult、业务异常、全局异常处理
├── config/                        借阅规则配置（借期、罚金、分页上限）
├── controller/                    REST 接口：book / reader / category / borrow / stats
├── domain/                        实体：Book、Category、Reader、BorrowRecord
├── dto/                           查询条件与请求体
├── mapper/                        MyBatis 接口（注解 + XML）
├── service/ + service/impl/       业务层，事务边界在这一层
├── task/                          超期扫描定时任务
└── vo/                            统计返回对象

src/main/resources
├── application.yml                H2 演示配置（默认）
├── application-mysql.yml          MySQL 配置（环境变量注入密码）
├── db/                            schema-h2.sql / schema-mysql.sql / data.sql
├── mapper/*.xml                   动态检索、联表统计 SQL
└── static/index.html              演示页面
```

## 部署到 Linux

打包成可执行 jar，配一个 systemd 服务即可，详见 [docs/deploy.md](docs/deploy.md)。

```bash
nohup java -jar library-management-system.jar --spring.profiles.active=mysql > app.log 2>&1 &
```

## 后续可以做的

- 引入 Redis 缓存热门图书与概览统计
- 读者登录与权限（管理员 / 馆员 / 读者）
- 预约、续借、罚金缴纳
- 批量导入图书（Excel）
- 用 Docker Compose 一键起 MySQL + 应用

## License

[MIT](LICENSE) © 2026 荀海洋
