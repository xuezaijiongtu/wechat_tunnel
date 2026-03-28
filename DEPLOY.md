# wechat-tunnel 部署文档

## 环境要求

| 项目 | 要求 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+（仅构建时需要） |
| Node.js | 16+（仅前端构建时需要） |
| 内存 | 512MB+ |
| 操作系统 | Linux / macOS / Windows |
| 网络 | 需要能访问 `ilinkai.weixin.qq.com`（iLink 服务） |

无需数据库、无需 Redis。

## 构建步骤

### 后端构建

```bash
cd wechat-tunnel
mvn clean package -DskipTests
```

构建产物：`wechat-tunnel-boot/target/wechat-tunnel-boot-1.0.0-SNAPSHOT.jar`

### 前端构建

```bash
cd wechat-tunnel-web
npm install
npm run build
```

构建产物：`wechat-tunnel-web/build/` 目录下的静态文件。

## 部署方式

### 方式一：开发模式（前后端分离）

分别启动后端和前端开发服务器，适合本地开发调试。

```bash
# 终端 1：启动后端
cd wechat-tunnel-boot
mvn spring-boot:run

# 终端 2：启动前端
cd wechat-tunnel-web
npm start
```

访问 `http://localhost:3000`，前端开发服务器会自动代理 API 和 WebSocket 请求到后端 8080 端口。

### 方式二：JAR 直接部署

适合简单部署场景，后端直接运行 JAR，前端通过 Nginx 提供静态文件服务。

```bash
# 1. 启动后端
WEB_PASSWORD=your_password java -jar wechat-tunnel-boot-1.0.0-SNAPSHOT.jar

# 2. 前端静态文件部署到 Nginx（见下方 Nginx 配置）
```

### 方式三：Nginx 反向代理（推荐生产部署）

将前端静态文件和后端 API 统一通过 Nginx 对外提供服务。

#### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /opt/wechat-tunnel-web/build;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 3600s;
    }
}
```

## 后端启动参数

通过环境变量或 JVM 参数覆盖默认配置：

```bash
# 环境变量方式
WEB_PASSWORD=my_secure_password \
java -jar wechat-tunnel-boot-1.0.0-SNAPSHOT.jar

# JVM 参数方式
java -jar wechat-tunnel-boot-1.0.0-SNAPSHOT.jar \
  --web.password=my_secure_password \
  --server.port=8080
```

## 后台运行（Linux）

使用 `nohup` 或 `systemd` 管理进程。

### nohup 方式

```bash
nohup java -jar wechat-tunnel-boot-1.0.0-SNAPSHOT.jar > app.log 2>&1 &
```

### systemd 方式

创建 `/etc/systemd/system/wechat-tunnel.service`：

```ini
[Unit]
Description=wechat-tunnel
After=network.target

[Service]
Type=simple
User=app
Environment=WEB_PASSWORD=your_password
ExecStart=/usr/bin/java -jar /opt/wechat-tunnel/wechat-tunnel-boot-1.0.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable wechat-tunnel
sudo systemctl start wechat-tunnel
```

## 健康检查

后端启动后可通过以下方式验证：

```bash
# 检查端口是否监听
curl -s http://localhost:8080/api/weixin/status

# 检查进程
ps aux | grep wechat-tunnel
```

## 注意事项

1. 生产环境务必通过环境变量 `WEB_PASSWORD` 设置强密码，不要使用默认密码
2. 服务重启后内存缓存清空，消息历史会丢失，这是设计预期
3. 后端需要能访问外网 `ilinkai.weixin.qq.com`，确保防火墙和代理配置正确
4. WebSocket 连接需要 Nginx 正确配置 `Upgrade` 和 `Connection` 头，否则实时消息无法工作
5. 微信 iLink 协议使用 Long-Polling（35 秒超时），Nginx 的 `proxy_read_timeout` 需要设置大于 35 秒
6. 一个微信号同时只能有一个 Bot Session，重复扫码会踢掉之前的连接
7. 前端构建产物是纯静态文件，可以部署到任意静态文件服务器（Nginx、CDN 等）
