# 接口文档

Base URL：`http://localhost:8080`

统一响应体：

```json
{ "code": 0, "message": "success", "data": {} }
```

| 场景 | HTTP 状态码 | code |
| --- | --- | --- |
| 成功 | 200 / 201 | 0 |
| 参数校验失败、业务规则不满足 | 400 | 400 |
| 资源不存在 | 400 | 404 |
| 未捕获异常 | 500 | 500 |

分页响应统一为：

```json
{ "total": 8, "page": 1, "size": 10, "list": [] }
```

---

## 1. 图书

### 1.1 分页检索

`GET /api/books`

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| keyword | string | 否 | 书名 / 作者 / ISBN 模糊匹配 |
| categoryId | long | 否 | 分类 ID |
| onlyAvailable | boolean | 否 | 只看有库存的 |
| page | int | 否 | 默认 1 |
| size | int | 否 | 默认 10，最大 100 |

```bash
curl "http://localhost:8080/api/books?keyword=Java&onlyAvailable=true&page=1&size=5"
```

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 2,
    "page": 1,
    "size": 5,
    "list": [
      {
        "id": 2,
        "isbn": "9787111376043",
        "title": "深入理解 Java 虚拟机",
        "author": "周志明",
        "publisher": "机械工业出版社",
        "categoryId": 1,
        "categoryName": "计算机",
        "totalStock": 3,
        "availableStock": 3,
        "status": 1,
        "createTime": "2026-01-06 09:00:00",
        "updateTime": "2026-01-06 09:00:00"
      }
    ]
  }
}
```

### 1.2 详情

`GET /api/books/{id}`

### 1.3 新增

`POST /api/books`

```json
{
  "isbn": "9787000000001",
  "title": "测试图书",
  "author": "测试作者",
  "publisher": "测试出版社",
  "categoryId": 1,
  "totalStock": 3
}
```

- `isbn` 唯一，重复返回 400「ISBN 已存在」
- 新增时可借库存自动等于馆藏总量
- 返回 201 + 图书对象

### 1.4 修改

`PUT /api/books/{id}`

调整 `totalStock` 时会校验：**在借册数 = 原馆藏 - 原可借**，新馆藏不能小于在借册数，可借库存自动重算。

### 1.5 删除

`DELETE /api/books/{id}`

该图书存在未归还记录时返回 400「该图书还有未归还的借阅记录，不能删除」。

---

## 2. 分类

`GET /api/categories`

```json
{ "code": 0, "message": "success", "data": [
  { "id": 1, "name": "计算机", "sort": 1 },
  { "id": 2, "name": "文学", "sort": 2 }
] }
```

---

## 3. 读者

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/readers?keyword=&page=&size=` | 按姓名 / 编号 / 手机号检索 |
| GET | `/api/readers/{id}` | 详情 |
| POST | `/api/readers` | 新增（读者编号唯一） |
| PUT | `/api/readers/{id}` | 修改 |
| DELETE | `/api/readers/{id}` | 删除（有未归还图书时拒绝） |

```json
{
  "readerNo": "R2026010",
  "name": "新读者",
  "phone": "13800000010",
  "type": 1,
  "status": 1,
  "maxBorrow": 5
}
```

`type`：1 学生，2 教师；`status`：1 正常，0 冻结（冻结后不能借书）。

---

## 4. 借还书

### 4.1 借书

`POST /api/borrows`

```json
{ "bookId": 2, "readerId": 1 }
```

成功返回 201：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 10,
    "bookId": 2,
    "readerId": 1,
    "bookTitle": "深入理解 Java 虚拟机",
    "readerName": "张三",
    "readerNo": "R2026001",
    "borrowTime": "2026-10-24 15:35:32",
    "dueTime": "2026-11-23 15:35:32",
    "status": "BORROWED",
    "overdueDays": 0,
    "fine": 0.00
  }
}
```

失败示例：

| 场景 | 返回 message |
| --- | --- |
| 读者被冻结 | 该读者已被冻结，无法借书 |
| 图书已下架 | 该图书已下架，无法借阅 |
| 无库存 | 该图书已无可借库存 |
| 超过上限 | 已达到最大借阅数量（5 本），请先归还 |
| 重复借阅 | 该读者已借阅此书且尚未归还 |

### 4.2 还书

`POST /api/borrows/{recordId}/return`

返回归还后的记录，`overdueDays`、`fine` 已计算好；重复调用返回 400「该记录已归还，请勿重复操作」。

### 4.3 借阅记录

`GET /api/borrows?status=BORROWED&readerId=1&page=1&size=10`

`status` 取值：`BORROWED`（在借）、`OVERDUE`（超期未还）、`RETURNED`（已归还）。

### 4.4 刷新超期状态

`POST /api/borrows/refresh-overdue`

```json
{ "code": 0, "message": "success", "data": 1 }
```

返回本次更新的记录数。定时任务每天 01:00 自动执行一次。

---

## 5. 统计

| 路径 | 说明 | 返回示例 |
| --- | --- | --- |
| `/api/stats/overview` | 概览 | `{"bookCount":8,"availableStock":26,"readerCount":4,"borrowingCount":4,"overdueCount":1}` |
| `/api/stats/hot-books?limit=5` | 热门图书 | `[{"bookId":1,"title":"Java 编程思想","author":"Bruce Eckel","borrowCount":2}]` |
| `/api/stats/categories` | 分类借阅分布 | `[{"categoryName":"计算机","borrowCount":5}]` |
| `/api/stats/monthly?months=6` | 近 6 个月借阅量 | `[{"year":2026,"month":5,"total":0}]` |
| `/api/stats/overdue?limit=20` | 超期未还明细 | `[{"recordId":3,"bookTitle":"活着","readerName":"王五","readerNo":"R2026003","dueTime":"2026-09-19 09:15:00","overdueDays":35,"fine":7.00}]` |

`/api/stats/hot-books` 的排序：借阅次数倒序，次数相同按图书 ID 升序。

`/api/stats/monthly` 会补齐没有借阅记录的月份（`total = 0`），返回值固定为 `months` 条。

---

## 6. 错误码约定

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 400 | 参数校验失败 / 业务规则不满足 |
| 404 | 图书、读者、借阅记录不存在 |
| 500 | 系统异常（日志里有完整堆栈） |
