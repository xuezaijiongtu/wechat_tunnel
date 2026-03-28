# wechat-tunnel 架构设计

## 1. 系统架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         微信用户                                 │
│                    (通过微信 App 发送消息)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ iLink Protocol (HTTPS)
                             │ Long-Polling (35s timeout)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              ilinkai.weixin.qq.com (腾讯 iLink 服务)             │
│  /ilink/bot/get_bot_qrcode                                      │
│  /ilink/bot/get_qrcode_status                                   │
│  /ilink/bot/getupdates                                          │
│  /ilink/bot/sendmessage                                         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS (出站连接)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  wechat-tunnel (Spring Boot)                    │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    Controller 层                          │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │   Auth      │  │   Weixin    │  │  WebSocket  │      │ │
│  │  │ Controller  │  │ Controller  │  │  Handler    │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  └───────────────────────────────────────────────────────────┘ │
│                             │                                   │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    Service 层                             │ │
│  │  ┌──────────────────┐  ┌──────────────────┐              │ │
│  │  │ WeixinILinkService│  │ MessageRouter   │              │ │
│  │  │  ├─ QR 登录       │  │ Service         │              │ │
│  │  │  ├─ Long-Polling  │  │                 │              │ │
│  │  │  └─ 发送消息      │  │                 │              │ │
│  │  └──────────────────┘  └──────────────────┘              │ │
│  │  ┌──────────────────┐                                     │ │
│  │  │ WebSocketService │                                     │ │
│  │  │  ├─ Session 管理  │                                     │ │
│  │  │  └─ 消息推送      │                                     │ │
│  │  └──────────────────┘                                     │ │
│  └──────────────────────────────────────────────────────┘ │
│                             │                                   │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    Cache 层 (内存)                        │ │
│  │  ┌──────────────────────────────────────────────────────┐│ │
│  │  │ InMemoryMessageCache                                 ││ │
│  │  │ Map<String, Queue<Message>>                          ││ │
│  │  │ 每个 userId 保留最近 50 条消息                        ││ │
│  │  └──────────────────────────────────────────────────────┘│ │
│  │  ┌──────────────────────────────────────────────────────┐│ │
│  │  │ ContextTokenCache                                    ││ │
│  │  │ Map<String, String>                                  ││ │
│  │  │ userId -> context_token (微信协议必需)                ││ │
│  │  └──────────────────────────────────────────────────────┘│ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ WebSocket + HTTP
                             ▼
┌─────────────────────────────────────────────────────────────
│                  wechat-tunnel-web (React)                      │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  页面层                                                    │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │   Login     │  │  WeixinQR   │  │    Chat     │      │ │
│  │  │    Page     │  │    Page     │  │    Page     │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  组件层                                                    │ │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │ │
│  │  │ SessionList │  │ MessageArea │  │  InputBox   │      │ │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  服务层                                                    │ │
│  │  ┌──────────────────┐  ┌──────────────────┐              │ │
│  │  │ WebSocketService │  │   API Service    │              │ │
│  │  └──────────────────┘  └──────────────────┘              │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 数据流图

#### 消息接收流程
```
微信用户发送消息
       │
       ▼
iLink 服务器
       │
       ▼
Long-Polling Loop (后端)
       │
       ├─ 解析消息
       ├─ 提取 from_user_id
       ├─ 提取 context_token → ContextTokenCache
       ├─ 提取文本内容
       │
       ▼
InMemoryMessageCache
       │
       ├─ 添加到 Queue<Message>
       ├─ 保留最近 50 条
       │
       ▼
WebSocketService
       │
       └─ 推送给所有在线 Web Session
              │
              ▼
          Web 界面显示
```

#### 消息发送流程
```
Web 用户输入消息
       │
       ▼
WebSocket 发送
       │
       ▼
WebSocketHandler (后端)
       │
       ├─ 解析目标 userId
       ├─ 解析消息内容
       │
       ▼
MessageRouterService
       │
       ├─ 从 ContextTokenCache 获取 token
       ├─ 添加到 InMemoryMessageCache
       │
       ▼
WeixinILinkService
       │
       ├─ 构造 sendMessage 请求
       ├─ 注入 context_token
       │
       ▼
iLink API (link/bot/sendmessage)
       │
       ▼
微信用户收到消息
```

## 2. 模块设计

### 2.1 后端模块结构

```
wechat-tunnel-boot/
└── src/main/java/com/xiaopeng/frd/wechat/
    ├── WechatTunnelApplication.java        # 启动类
    ├── controller/
    │   ├── AuthController.java             # 密码登录
    │   ├── WeixinController.java           # 微信 QR 登录
    │   └── WebSocketHandler.java           # WebSocket 处理
    └── config/
        ├── WebSocketConfig.java            # WebSocket 配置
        └── CorsConfig.java                 # 跨域配置

wechat-tunnel-service/
└── src/main/java/com/xiaopeng/frd/wechat/
    ├── service/
    │   ├── WeixinILinkService.java         # iLink 协议客户端
    │   ├── MessageRouter       # 消息路由
    │   └── WebSocketService.java           # WebSocket 管理
    ├── cache/
    │   ├── InMemoryMessageCache.java       # 消息缓存
    │   └── ContextTokenCache.java          # Token 缓存
    ├── dto/
    │   ├── LoginDTO.java                   # 登录请求
    │   ├── QRCodeDTO.java                  # 二维码响应
    │   ├── WeixinMessageDTO.java           # 微信消息
    │   └── WebMessageDTO.java              # Web 消息
    ├── vo/
    │   ├── MessageVO.java                  # 消息视图
    │   ├── SessionVO.java                  # 会话视图
    │   └── ConnectionStatusVO.java         # 连接状态
    ├── enums/
    │   ├── MessageTypeEnum.java            # 消息类型
    │   └── ConnectionStatusEnum.java       # 连接状态
    └── common/
        └── WebSocketMessage.java           # WebSocket 消息封装
```

### 2.2 前端模块结构

```
wechat-tunnel-web/
└── src/
    ├── pages/
    │   ├── Login/
    │   │   ├── index.tsx                   # 登录页面
    │   │   └── index.less
    │   ├── WeixinQRCode/
    │   │   ├── index.tsx                   # 扫码页面
    │   │   └── index.less
    │   └── Chat/
    │       ├── index.tsx                   # 聊天主页面
    │       ├── SessionList.tsx         列表
    │       ├── MessageArea.tsx             # 消息区域
    │       ├── InputBox.tsx                # 输入框
    │       └── index.less
    ├── services/
    │   ├── websocket.ts                    # WebSocket 客户端
    │   └── api.ts                          # HTTP API
    ├── hooks/
    │   ├── useWebSocket.ts                 # WebSocket Hook
    │   └── useMessages.ts                  # 消息管理 Hook
    ├── types/
    │   └── message.ts                      # 类型定义
    └── utils/
        └── format.ts                       # 工具函数
```

## 3. 核心类设计

### 3.1 WeixinILinkService

```java
/**
 * 微信 iLink 协议服务
 */
@Slf4j
@Service
public class WeixinILinkService {

    private static final String BASE_URL = "https://ilinkai.weixin.qq.com";
    private String botToken;
    private String getUpdatesBuf = "";  // 游标
    private volatile boolean polling = false;

    /**
     * 获取 QR 登录二维码
     */
    public QRCodeDTO getQRCode() {
        // GET /ilink/bot/get_bot_qrcode?bot_type=3
    }

    /**
     * 轮询 QR 扫码状态
     */
    public QRStatusDTO pollQRStatus(String qrcode) {
        // GET /ilink/bot/get_qrcode_status?qrcode={qrcode}
        // 返回: scanned / confirmed / expired
    }

    /**
     * 启动 Long-Polling 循环
     */
    @Async
    public void startPolling() {
        polling = true;
        while (polling) {
            try {
                GetUpdatesResponse resp = getUpdates();
                if (resp.getMsgs() != null) {
                    for (WeixinMessageDTO msg : resp.getMsgs()) {
                        handleInboundMessage(msg);
                    }
                }
                getUpdatesBuf = resp.getGetUpdatesBuf();
            } catch (Exception e) {
                log.error("Long-polling error", e);
                backoffDelay();
            }
        }
    }

    /**
     * 停止 Long-Polling
     */
    public void stopPolling() {
        polling = false;
    }

    /**
     * 拉取消息更新
     */
    private GetUpdatesResponse getUpdates() {
        // POST /ilink/bot/getupdates
        // Body: { "get_updates_buf": cursor, "base_info": {...} }
    }

    /**
     * 发送消息到微信
     */
    public void sendMessage(String toUserId, String text, String contextToken) {
        // POST /ilink/bot/sendmessage
        // Body: { "msg": { "to_user_id", "context_token", "item_list": [...] } }
    }

    /**
     * 生成防重放 Header
     */
   vate String generateUIN() {
        int uint32 = new Random().nextInt();
        return Base64.getEncoder().encodeToString(String.valueOf(uint32).getBytes());
    }

    /**
     * 构造认证 Headers
     */
    private Map<String, String> authHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("AuthorizationType", "ilink_bot_token");
        headers.put("Authorization", "Bearer " + botToken);
        headers.put("X-WECHAT-UIN", generateUIN());
        return headers;
    }
}
### 3.2 MessageRouterService

```java
/**
 * 消息路由服务
 */
@Slf4j
@Service
public class MessageRouterService {

    private final InMemoryMessageCache messageCache;
    private final ContextTokenCache tokenCache;
    private final WebSocketService webSocketService;
    private final WeixinILinkService weixinService;

    /**
     * 处理来自微信的消息
     */
    public void handleWeixinMessage(WeixinMessageDTO msg) {
        String userId = msg.getFromUserId();
        String text = extractText(msg);
        String contextToken = msg.getContextToken();

        // 缓存 context_token（关键！）
        tokenCache.put(userId, contextToken);

        // 构造消息 VO
        MessageVO messageVO = MessageVO.builder()
            .userId(userId)
            .content(text)
            .direction("inbound")
            .timestamp(System.currentTimeMillis())
            .build();

        // 添加到缓存
        messageCache.addMessage(userId, messageVO);

        // 推送到 Web
        webSocketService.broadcastMessage(messageVO);
    }

    /**
     * 处理来自 Web 的消息
     */
    public void handleWebMessage(WebMessageDTO msg) {
        String userId = msg.getUserId();
        String text = msg.getContent();

        // 获取 context_token
        String contextToken = tokenCache.get(userId);
        if (contextToken == null) {
            log.warn("No context_token for userId: {}", userId);
            return;
        }

        // 构造消息 VO
        MessageVO messageVO = MessageVO.builder()
            .userId(userId)
            .content(text)
            .direction("outbound")
            .timestamp(System.currentTimeMillis())
            .build();

        // 添加到缓存essageCache.addMessage(userId, messageVO);

        // 发送到微信
        weixinService.sendMessage(userId, text, contextToken);

        // 回显到 Web（确认发送成功）
        webSocketService.broadcastMessage(messageVO);
    }
}
```

### 3.3 InMemoryMessageCache

```java
/**
 * 内存消息缓存
 */
@Component
public class InMemoryMessageCache {

    private static final int MAX_SIZE_PER_USER = 50;

    // userId -> Queue<MessageVO>
    private final Map<String, Queue<MessageVO>> cache = new ConcurrentHashMap<>();

    /**
     * 添加消息
     */
    public void addMessage(String userId, MessageVO message) {
        Queue<MessageVeue = cache.computeIfAbsent(
            Id,
            k -> new ConcurrentLinkedQueue<>()
        );

        queue.offer(message);

        // 保留最近 50 条
        while (queue.size() > MAX_SIZE_PER_USER) {
            queue.poll();
        }
    }

    /**
     * 获取用户的所有消息
     */
    public List<MessageVO> getMessages(String userId) {
        Queue<MessageVO> queue = cache.get(userId);
        return queue == null ? Collections.emptyList() : new ArrayList<>(queue);
    }

    /**
     * 获取所有会话
     */
    public List<SessionVO> getAllSessions() {
        return cache.entrySet().stream()
            .map(entry -> {
                String userId = entry.getKey();
                Queue<MessageVO> messages = entry.getValue();
                MessageVO lastMsg = ((ConcurrentLinkedQueue<MessageVO>) messages).peek();

                return SessionVO.builder()
                    .userId(userId)
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                    .lastMessageTime(lastMsg != null ? lastMsg.getTimestamp() : 0L)
                    .unreadCount(0)  // 简化版不计算未读
                    .build();
            })
            .sorted(Comparator.comparing(SessionVO::getLastMessageTime).reversed())
            .collect(Collectors.toList());
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
    }
}
```

### 3.4 WebSocketHandler

```java
/**
 * WebSocket 处理器
 */
@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;
    private final MessageRouterService messageRouter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) og.info("WebSocket connected: {}", session.getId());
        webSocketService.addSession(session);

        // 发送历史消息
        sendHistoryMessages(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        WebMessageDTO webMsg = JSON.parseObject(payload, WebMessageDTO.class);

        // 路由到消息处理
        messageRouter.handleWebMessage(webMsg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket closed: {}", session.getId());
        webSocketService.removeSession(session);
    }

    private void sendHistoryMessages(WebSocketSession session) {
        // 发送所有会话的历史消息
        // ...
    }
}
```

## 4. 配置设计

### 4.1 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: wechat-tunnel

# Web 登录密码
web:
  password: ${WEB_PASSWORD:admin123}

# 微信配置
weixin:
  base-url: https://ilinkai.weixin.qq.com
  long-polling-timeout: 35000
  bot-type: 3

# 消息缓存配置
cache:
  message:
    max-size-per-user: 50

# WebSocket 配置
websocket:
  endpoint: /ws/chat
  allowed-origins: "*"

# 日志配置
logging:
  level:
    com.xiaopeng.frd.wechat: DEBUG
```

### 4.2 WebSocketConfig

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}
```

## 5. API 设计

### 5.1 REST API

#### 登录
```
POST /api/auth/login
Request:
{
  "password": "admin123"
}

Response:
{
  "success": true,
  "token": "session-token-xxx"
}
```

#### 获取 QR 二维码
```
GET /api/weixin/qr

Response:
{
  "qrcode": "qrcode-string",
  "qrcodeImage": "data:image/png;base64,..."
}
```

#### 轮询 QR 状态
```
GET /api/weixin/qr/status?qrcode={qrcode}

Response:
{
  "status": "scanned" | "confirmed" | "expired",
  "botToken": "xxx"  // 仅 confirmed 时返回
}
```

#### 获取连接状态
```
GET /api/weixin/status

Response:
{
  "connected": true,
  "connectedAt": 1234567890
}
```

#### 获取会话列表
```
GET /api/sessions

Response:
{
  "sessions": [
    {
      "userId": "xxx@im.wechat",
      "lastMessage": "你好",
      "lastMessageTi7890,
      "unreadCount": 0
    }
  ]
}
```

#### 获取消息历史
```
GET /api/messages?userId={userId}

Response:
{
  "messages": [
    {
      "userId": "xxx@im.wechat",
      "content": "你好",
      "direction": "inbound",
      "timestamp": 1234567890
    }
  ]
}
```

### 5.2 WebSocket 协议

#### 客户端 → 服务端（发送消息）
```json
{
  "type": "send_message",
  "userId": "xxx@im.wechat",
  "content": "你好"
}
```

#### 服务端 → 客户端（接收消息）
```json
{
  "type": "new_message",
  "message":   "userId": "xxx@im.wechat",
    "content": "你好",
    "direction": "inbound",
    "timestamp": 1234567890
  }
}
```

#### 服务端 → 客户端（连接状态变化）
```json
{
  "type": "connection_status",
  "connected": true
}
```

## 6. 错误处理

### 6.1 微信协议错误

| errcode | 含义 | 处理策略 |
|---------|------|----------|
| -14 | Session 过期 | 停止 Polling，通知 Web 重新扫码 |
| 网络瞬断 | 连接中断 | 指数退避重试（1s, 2s, 4s, 8s, 最大 30s） |
| QR 超时 | 扫码超时 | 返回 expired 状态，提示用户重新获取 |

### 6.2 WebSocket 错误

- 连接断开：自动重连（前端实现）
- 消息发送失败：显示错误提示
- 认证失败：跳转到登录页

## 7. 性能考虑

### 7.1 内存管理
- 每个用户最多缓存 50 条消息
- 假设 100 个活跃用户，每条消息 1KB，总内存占用约 5MB
- 可接受的内存开销

### 7.2 并发处理
- Long-Polling 使用单独线程（@Async）
- WebSocket 连接使用 Spring 默认线程池
- 消息缓存使用 ConcurrentHashMap，线程安全

### 7.3 网络优化
- Long-Polling 超时 35s（iLink 协议规定）
- WebSocket 心跳检测（60s）
- HTTP 请求超时 30s

## 8. 安全考虑

### 8.1 认证
- Web 登录使用简单密码（配置文件）
- 密码通过环境变量注入，不硬编码
- Session Token 存储在内存中

### 8.2 数据安全
- 消息不持久化，重启后清空
- bot_token 存储在内存中，不落盘
- WebSocket 可配置 WSS（生产环境）

### 8.3 访问控制
- 无白名单机制（所有微信用户都可以发消息）
- 可在 Phase 2 添加白名单功能

## 9. 部署架构

```
┌─────────────────────────────────────┐
│         Nginx (可选)                │
│  ├─ HTTP → 8080                     │
│  └─ WebSocket → 8080/ws/chat        │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│    wechat-tunnel (Spring Boot)      │
│    java -jar wechat-tunnel.jar      │
│    端口: 8080                        │
└─────────────────────────────────────┘
```

### 部署要求
- Java 8+
- 内存: 512MB+
- 无需数据库
- 无需 Redis
- 需要公网访问（前端访问后端）

## 10. 监控与日志

### 10.1 关键日志
- 微信连接状态变化
- Long-Polling 错误
- 消息收发记录
- WebSocket 连接/断开

### 10.2 监控指标
- 在线 WebSocket 连接数
- 缓存消息总数
- Long-Polling 循环状态
- 微信连接状态

## 11. 测试策略

### 11.1 单元测试
- InMemoryMessageCache 测试
- ContextTokenCache 测试
- MessageRouterService 测试

### 11.2 集成测试
- Mock iLink 服务端
- 测试完整消息流程
- 测试错误处理

### 11.3 手动测试
- QR 扫码登录
- 消息收发
- 刷新页面（验证缓存）
- 服务重启（验证重连）
