# 部署到 Linux 服务器

以 CentOS 7 / openEuler / Ubuntu 20.04 为例，目标是把应用以 `systemd` 服务的方式跑起来。

## 1. 准备环境

```bash
# 确认 JDK 17
java -version

# 没有的话（CentOS / openEuler）
yum install -y java-17-openjdk java-17-openjdk-devel
# Ubuntu / Debian
apt install -y openjdk-17-jre-headless
```

## 2. 初始化 MySQL

```sql
CREATE DATABASE library DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'library'@'%' IDENTIFIED BY '强密码';
GRANT ALL PRIVILEGES ON library.* TO 'library'@'%';
FLUSH PRIVILEGES;
```

```bash
mysql -ulibrary -p library < src/main/resources/db/schema-mysql.sql
mysql -ulibrary -p library < src/main/resources/db/data.sql   # 演示数据，生产可不执行
```

## 3. 打包并上传

本地：

```bash
mvn clean package -DskipTests
# 产物：target/library-management-system.jar
```

上传：

```bash
scp target/library-management-system.jar root@服务器IP:/opt/library/
```

## 4. 配置环境变量

`/opt/library/library.env`（权限 `chmod 600`，只给 root 读）：

```bash
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=library
DB_USER=library
DB_PASSWORD=强密码
```

> `application-mysql.yml` 里的连接信息全部从环境变量读取，**不要把密码写进配置文件或提交到 Git**。

## 5. systemd 服务

`/etc/systemd/system/library.service`：

```ini
[Unit]
Description=Library Management System
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/library
EnvironmentFile=/opt/library/library.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m \
  -jar /opt/library/library-management-system.jar \
  --spring.profiles.active=mysql \
  --server.port=8080
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=append:/opt/library/logs/app.log
StandardError=append:/opt/library/logs/error.log

[Install]
WantedBy=multi-user.target
```

```bash
mkdir -p /opt/library/logs
systemctl daemon-reload
systemctl enable --now library
systemctl status library
journalctl -u library -f
```

## 6. 快速验证

```bash
curl http://127.0.0.1:8080/api/stats/overview
curl "http://127.0.0.1:8080/api/books?size=3"
curl -X POST http://127.0.0.1:8080/api/borrows \
     -H "Content-Type: application/json" \
     -d '{"bookId":2,"readerId":1}'
```

## 7. 用 Nginx 反向代理（可选）

```nginx
server {
    listen 80;
    server_name library.example.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 8. 出问题时怎么排查

按这个顺序，基本能覆盖 90% 的「服务起不来」：

1. `ps -ef | grep java` —— 进程在不在
2. `netstat -tunlp | grep 8080` —— 端口有没有起来
3. `tail -200 /opt/library/logs/app.log` —— 日志里最上面那条 `Caused by` 才是根因
4. `df -h` / `free -h` —— 磁盘、内存是不是满了
5. 确认 JDK 版本（必须是 17）、jar 文件权限、`--spring.profiles.active=mysql` 有没有带上
6. 本机 `curl 127.0.0.1:8080/api/stats/overview` 通了但外网不通，就看防火墙：

```bash
firewall-cmd --zone=public --add-port=8080/tcp --permanent
firewall-cmd --reload
```

## 9. 日常运维

```bash
# 重启
systemctl restart library

# 看最近 200 行日志
tail -200 /opt/library/logs/app.log

# 备份数据库
mysqldump -ulibrary -p library > /opt/backup/library_$(date +%F).sql
```
