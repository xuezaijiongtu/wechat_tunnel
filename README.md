# wechat-tunnel

微信消息互通工具，基于腾讯官方 iLink 协议（微信 ClawBot 插件）实现 Web 端与微信用户的实时双向聊天。纯转发模式，无数据库依赖，轻量部署。

## 功能特性

- ✅ 微信 QR 扫码登录（iLink 官方协议）
- ✅ Long-Polling 实时消息接收
- ✅ WebSocket 双向实时通信
- ✅ Web 聊天界面（会话列表 + 消息气泡 + 输入框）
- ✅ 微信表情自动转换（`[微笑]` → 😊）
- ✅ UTF-8 中文完美支持
- ✅ 用户ID格式化显示（`微信用户 o9cq802k`）
- ✅ 优化的消息样式（圆角气泡、阴影、发送者名称）
- ✅ 内存消息缓存（每用户最近 50 条，刷新不丢失）
- ✅ 无数据库、无 Redis，单 JAR 部署

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8 + Spring Boot 2.7.18 + WebSocket |
| 前端 | React 19 + TypeScript + Ant Design 5 |
| 协议 | 腾讯 iLink Protocol（HTTPS + Long-Polling） |
| 构建 | Maven 多模块 + Create React App |

## 项目结构

```
wechat-tunnel/
├── wechat-tunnel-boot/        # 启动模块（Controller、WebSocket、Config）
├── wechat-tunnel-service/     # 业务模块（Service、Cache、DTO、VO）
├── wechat-tunnel-web/         # 前端模块（React）
├── sql/                       # 数据库脚本（本项目为空，无数据库）
└── pom.xml                    # 父 POM
```

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- Node.js 16+（前端开发）
- npm 8+

### 后端启动

```bash
# 1. 编译打包
cd wechat-tunnel
mvn clean install -DskipTests

# 2. 启动服务
cd wechat-tunnel-boot
mvn spring-boot:run
```

后端默认监听 `http://localhost:8080`。

### 前端启动

```bash
# 1. 安装依赖
cd wechat-tunnel-web
npm install

# 2. 启动开发服务器
npm start
```

前端默认监听 `http://localhost:1999`，已配置代理转发 `/api` 和 `/ws` 到后端 8080 端口。

### 访问

打开浏览器访问 http://localhost:1999

## 使用说明

### 1. 微信扫码登录

打开浏览器访问 http://localhost:1999，页面会自动显示微信登录二维码。
<img width="3024" height="1542" alt="login" src="https://github.com/user-attachments/assets/87137537-c7e7-4892-a228-d26c20790066" />

使用微信扫描页面上的二维码，在手机上确认登录。扫码确认后系统自动跳转到聊天界面并启动消息轮询。

> 注意：微信账号需要具备 ClawBot（iLink）资格才能扫码成功。

### 2. 聊天

扫码成功后进入聊天界面：
![1d72662601327d2fe03d1daf1aa79856](https://github.com/user-attachments/assets/1025f428-2ca4-43e6-a26d-d37196a1449f)
<img width="2996" height="1502" alt="a8798ae4056944bcea7cfbf3430a4ef0" src="https://github.com/user-attachments/assets/7e5a2007-7acc-487f-ac5d-eda0fc40b4d5" />

- **左侧会话列表**：显示所有与你聊天的微信用户，显示最后一条消息和未读数
- **右侧消息区域**：点击会话查看聊天记录，支持表情显示
- **底部输入框**：发送文本消息，支持回车发送

### 3. 表情支持

系统自动将微信表情代码转换为 emoji 显示：

- `[微笑]` → 😊
- `[撇嘴]` → 😣
- `[爱心]` → ❤️
- `[强]` → 👍
- 支持 80+ 常用微信表情

发送消息时也可以直接输入表情代码，微信端会正常显示。

## 配置说明

配置文件位于 `wechat-tunnel-boot/src/main/resources/application.yml`：

```yaml
server:
  port: 8080                              # 服务端口

web:
  password: ${WEB_PASSWORD:admin123}      # Web 登录密码（建议通过环境变量设置）

weixin:
  base-url: https://ilinkai.weixin.qq.com # iLink 服务地址
  long-polling-timeout: 35000             # Long-Polling 超时时间（毫秒）
  bot-type: 3                             # Bot 类型

cache:
  message:
    max-size-per-user: 50                 # 每用户最大缓存消息数

websocket:
  endpoint: /ws/chat                      # WebSocket 端点
  allowed-origins: "*"                    # 允许的跨域来源

logging:
  level:
    com.xiaopeng.frd.wechat: DEBUG        # 日志级别
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `WEB_PASSWORD` | Web 登录密码 | `admin123` |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 密码登录 |
| GET | `/api/weixin/qr` | 获取微信扫码二维码 |
| GET | `/api/weixin/qr/status?qrcode={qrcode}` | 轮询扫码状态 |
| GET | `/api/weixin/status` | 获取微信连接状态 |
| GET | `/api/sessions` | 获取会话列表 |
| GET | `/api/messages?userId={userId}` | 获取消息历史 |
| WebSocket | `/ws/chat` | 实时消息通道 |

## 架构简图

```
微信用户 ──iLink Protocol──▶ iLink 服务器
                                │
                          HTTPS Long-Polling
                                │
                                ▼
                     wechat-tunnel (Spring Boot)
                     ├─ WeixinILinkService (协议客户端)
                     ├─ MessageRouterService (消息路由)
                     ├─ InMemoryMessageCache (内存缓存)
                     └─ WebSocketService (推送服务)
                                │
                           WebSocket
                    │
                                ▼
                     wechat-tunnel-web (React)
                     └─ 聊天界面
```

## 常见问题

### Q: 扫码后提示失败？

确认微信账号已开通 ClawBot（iLink）资格。该功能目前处于灰测阶段，并非所有微信号都支持。

### Q: 刷新页面后消息还在吗？

在服务运行期间，每个用户最近 50 条消息会缓存在内存中，刷新页面可以看到。服务重启后缓存清空。

### Q: 支持群聊吗？

不支持。iLink 协议目前仅支持私聊消息。

### Q: 支持图片/文件/语音消息吗？

当前版本仅支持文本消息和表情。图片、文件、语音等富媒体消息计划在后续版本支持。

### Q: 为什么有些消息收不到或发不出去？

可能的原因：
1. **微信API限流**：短时间内发送过多消息可能被限制，建议适当控制发送频率
2. **Long-Polling延迟**：接收消息依赖轮询机制，可能有1-2秒延迟
3. **Session过期**：长时间未操作可能导致session失效，需要重新扫码登录

查看后端日志可以获取详细错误信息：
```bash
tail -f /tmp/wechat-backend.log | grep -E "ERROR|发送消息响应"
```

### Q: 中文显示乱码怎么办？

已修复UTF-8编码问题。如果仍有乱码，请确保：
1. 后端使用最新版本代码
2. 重启后端服务
3. 刷新浏览器页面

### Q: 可以多人同时使用吗？

当前为单用户模式，同一时间只能有一个微信账号连接。多个 Web 用户可以同时查看和发送消息（共享同一个微信会话）。

### Q: 密码如何修改？

通过环境变量 `WEB_PASSWORD` 设置，例如：

```bash
WEB_PASSWORD=your_password java -jar wechat-tunnel-boot.jar
```

### Q: 前端生产环境如何部署？

```bash
cd wechat-tunnel-web
npm run build
```

将 `build/` 目录下的静态文件部署到 Nginx 或其他 Web 服务器，配置反向代理指向后端服务即可。详见 [DEPLOY.md](./DEPLOY.md)。

## 已知限制

- 仅支持私聊文本消息
- 单微信账号模式
- 消息不持久化（内存缓存，重启清空）
- 需要微信 ClawBot 资格
- 一个微信号同时只能有一个 Bot Session

## License

Internal use only.
