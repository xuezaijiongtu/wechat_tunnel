# wechat-tunnel 实施任务

## 任务概览

本文档将项目分解为可执行的任务，按照依赖关系组织。每个任务都是独立的工作单元，可以由开发者逐个完成。

## 任务依赖图

```
Phase 1: 项目初始化
├─ Task 1.1: 创建项目骨架
└─ Task 1.2: 配置开发环境

Phase 2: 后端核心功能
├─ Task 2.1: 实现 iLink 协议客户端
├─ Task 2.2: 实现消息缓存
├─ Task 2.3: 实现消息路由服务
└─ Task 2.4: 实现 WebSocket 服务

Phase 3: 后端 API
├─ Task 3.1: 实现认证接口
├─ Task 3.2: 实现微信登录接口
└─ Task 3.3: 实现消息查询接口

Phase 4: 前端基础
├─ Task 4.1: 创建前端项目
├─ Task 4.2: 实现路由和布局
└─ Task 4.3: 实现 WebSocket 客户端

Phase 5: 前端页面
├─ Task 5.1: 实现登录页面
├─ Task 5.2: 实现微信扫码页面
└─ Task 5.3: 实现聊天页面

Phase 6: 集成测试
├─ Task 6.1: 端到端测试
└─ Task 6.2: 文档完善
```

---

## Phase 1: 项目初始化

### Task 1.1: 创建项目骨架

**目标**：创建 Maven 多模块项目结构

**步骤**：
1. 创建父 POM（wechat-tunnel）
2. 创建子模块：wechat-tunnel-boot
3. 创建子模块：wechat-tunnel-service
4. 配置模块依赖关系

**产物**：
```
wechat-tunnel/
├── pom.xml                      # 父 POM
├── wechat-tunnel-boot/
│   └── pom.xml
└── wechat-tunnel-service/
    └── pom.xml
```

**验收标准**：
- [x] Maven 构建成功
- [x] 模块依赖正确

**预估时间**：30 分钟

---

### Task 1.2: 配置开发环境

**目标**：配置 Spring Boot 和基础依赖

**步骤**：
1. 在 boot 模块添加 Spring Boot 依赖
2. 在 service 模块添加工具类依赖（Lombok、Hutool）
3. 创建 application.yml 配置文件
4. 创建启动类 WechatTunnelApplication

**产物**：
- `wechat-tunnel-boot/src/main/resources/application.yml`
- `WechatTunnelApplication.java`

**验收标准**：
- [x] Spring Boot 应用可以启动
- [x] 日志正常输出

**预估时间**：30 分钟

---

## Phase 2: 后端核心功能

### Task 2.1: 实现 iLink 协议客户端

**目标**：实现微信 iLink 协议的核心功能

**依赖**：Task 1.2

**步骤**：
1. 创建 DTO 类
   - `WeixinMessageDTO.java`
   - `QRCodeDTO.java`
   - `GetUpdatesResponse.java`
2. 创建 `WeixinILinkService.java`
   - 实现 `getQRCode()` 方法
   - 实现 `pollQRStatus()` 方法
   - 实现 `getUpdates()` 方法
   - 实现 `sendMessage()` 方法
   - 实现 `startPolling()` 异步循环
   - 实现 `stopPolling()` 方法
3. 实现 HTTP 请求工具（使用 RestTemplate 或 OkHttp）
4. 实现认证 Header 生成（UIN、Authorization）

**产物**：
- `service/WeixinILinkService.java`
- `dto/WeixinMessageDTO.java`
- `dto/QRCodeDTO.java`

**验收标准**：
- [x] 可以获取 QR 二维码
- [x] 可以轮询扫码状态
- [x] 可以启动 Long-Polling 循环
- [x] 可以发送消息到微信

**预估时间**：4 小时

---

### Task 2.2: 实现消息缓存

**目标**：实现内存消息缓存和 Token 缓存

**依赖**：无

**步骤**：
1. 创建 `MessageVO.java`
2. 创建 `SessionVO.java`
3. 创建 `InMemoryMessageCache.java`
   - 实现 `addMessage()` 方法
   - 实现 `getMessages()` 方法
   - 实现 `getAllSessions()` 方法
   - 实现消息数量限制（50 条）
4. 创建 `ContextTokenCache.java`
   - 实现 `put()` 方法
   - 实现 `get()` 方法

**产物**：
- `cache/InMemoryMessageCache.java`
- `cache/ContextTokenCache.java`
- `vo/MessageVO.java`
- `vo/SessionVO.java`

**验收标准**：
- [x] 可以添加和获取消息
- [x] 消息数量限制生效
- [x] 可以获取所有会话
- [x] Token 缓存正常工作

**预估时间**：2 小时

---

### Task 2.3: 实现消息路由服务

**目标**：实现消息在微信和 Web 之间的路由

**依赖**：Task 2.1, Task 2.2

**步骤**：
1. 创建 `WebMessageDTO.java`
2. 创建 `MessageRouterService.java`
   - 实现 `handleWeixinMessage()` 方法
     - 提取 userId、text、contextToken
     - 缓存 contextToken
     - 添加到消息缓存
     - 推送到 WebSocket
   - 实现 `handleWebMessage()` 方法
     - 获取 contextToken
     - 添加到消息缓存
     - 发送到微信
     - 回显到 Web
3. 集成 WeixinILinkService 和 InMemoryMessageCache

**产物**：
- `service/MessageRouterService.java`
- `dto/WebMessageDTO.java`

**验收标准**：
- [x] 微信消息可以路由到缓存
- [x] Web 消息可以发送到微信
- [x] contextToken 正确缓存和使用

**预估时间**：2 小时

---

### Task 2.4: 实现 WebSocket 服务

**目标**：实现 WebSocket 服务端

**依赖**：Task 2.3

**步骤**：
1. 添加 Spring WebSocket 依赖
2. 创建 `WebSocketConfig.java`
3. 创建 `WebSocketHandler.java`
   - 实现 `afterConnectionEstablished()` 方法
   - 实现 `handleTextMessage()` 方法
   - 实现 `afterConnectionClosed()` 方法
4. 创建 `WebSocketService.java`
   - 实现 Session 管理
   - 实现 `broadcastMessage()` 方法
   - 实现 `sendToSession()` 方法
5. 创建 `WebSocketMessage.java` 封装类

**产物**：
- `config/WebSocketConfig.java`
- `controller/WebSocketHandler.java`
- `service/WebSocketService.java`
- `common/WebSocketMessage.java`

**验收标准**：
- [x] WebSocket 可以连接
- [x] 可以接收客户端消息
- [x] 可以推送消息到客户端
- [x] 连接断开正常处理

**预估时间**：3 小时

---

## Phase 3: 后端 API

### Task 3.1: 实现认证接口

**目标**：实现简单密码登录

**依赖**：Task 1.2

**步骤**：
1. 创建 `LoginDTO.java`
2. 创建 `AuthController.java`
   - 实现 `POST /api/auth/login` 接口
   - 验证密码（从配置文件读取）
   - 生成 Session Token（UUID）
   - 返回 Token
3. 在 application.yml 添加密码配置

**产物**：
- `controller/AuthController.java`
- `dto/LoginDTO.java`

**验收标准**：
- [x] 正确密码可以登录
- [x] 错误密码返回 401
- [x] 返回有效 Token

**预估时间**：1 小时

---

### Task 3.2: 实现微信登录接口

**目标**：实现微信 QR 扫码登录接口

**依赖**：Task 2.1

**步骤**：
1. 创建 `WeixinController.java`
   - 实现 `GET /api/weixin/qr` 接口
     - 调用 WeixinILinkService.getQRCode()
     - 返回二维码数据
   - 实现 `GET /api/weixin/qr/status` 接口
     - 调用 WeixinILinkService.pollQRStatus()
     - 返回扫码状态
     - 如果 confirmed，启动 Long-Polling
   - 实现 `GET /api/weixin/status` 接口
     - 返回当前连接状态
2. 创建 `ConnectionStatusVO.java`

**产物**：
- `controller/WeixinController.java`
- `vo/ConnectionStatusVO.java`

**验收标准**：
- [x] 可以获取 QR 二维码
- [x] 可以轮询扫码状态
- [x] 扫码成功后启动 Long-Polling
- [x] 可以查询连接状态

**预估时间**：2 小时

---

### Task 3.3: 实现消息查询接口

**目标**：实现会话和消息查询接口

**依赖**：Task 2.2

**步骤**：
1. 在 `WeixinController.java` 添加接口
   - 实现 `GET /api/sessions` 接口
     - 调用 InMemoryMessageCache.getAllSessions()
     - 返回会话列表
   - 实现 `GET /api/messages` 接口
     - 调用 InMemoryMessageCache.getMessages(userId)
     - 返回消息历史

**产物**：
- 更新 `controller/WeixinController.java`

**验收标准**：
- [x] 可以获取会话列表
- [x] 可以获取指定用户的消息历史

**预估时间**：1 小时

---

## Phase 4: 前端基础

### Task 4.1: 创建前端项目

**目标**：创建 React + Ant Design 前端项目

**依赖**：无

**步骤**：
1. 使用 create-react-app 创建项目（TypeScript 模板）
2. 安装依赖
   - antd
   - @ant-design/pro-components
   - axios
   - react-router-dom
3. 配置 tsconfig.json
4. 配置代理（proxy 到后端 8080）

**产物**：
```
wechat-tunnel-web/
├── package.json
├── tsconfig.json
└── src/
```

**验收标准**：
- [x] 项目可以启动
- [x] Ant Design 样式正常
- [x] 代理配置生效

**预估时间**：30 分钟

---

### Task 4.2: 实现路由和布局

**目标**：配置路由和基础布局

**依赖**：Task 4.1

**步骤**：
1. 创建路由配置
   - `/login` - 登录页
   - `/weixin-qr` - 扫码页
   - `/chat` - 聊天页
2. 创建 `App.tsx` 主组件
3. 配置路由守卫（检查登录状态）

**产物**：
- `src/App.tsx`
- `src/routes.tsx`

**验收标准**：
- [x] 路由跳转正常
- [x] 未登录跳转到登录页

**预估时间**：1 小时

---

### Task 4.3: 实现 WebSocket 客户端

**目标**：实现 WebSocket 客户端和消息管理

**依赖**：Task 4.1

**步骤**：
1. 创建 `services/websocket.ts`
   - 实现 WebSocket 连接
   - 实现消息发送
   - 实现消息接收回调
   - 实现自动重连
2. 创建 `hooks/useWebSocket.ts`
   - 封装 WebSocket 逻辑
   - 管理连接状态
3. 创建 `hooks/useMessages.ts`
   - 管理消息列表
   - 管理会话列表

**产物**：
- `src/services/websocket.ts`
- `src/hooks/useWebSocket.ts`
- `src/hooks/useMessages.ts`

**验收标准**：
- [x] WebSocket 可以连接
- [x] 可以发送和接收消息
- [x] 断线自动重连

**预估时间**：2 小时

---

## Phase 5: 前端页面

### Task 5.1: 实现登录页面

**目标**：实现密码登录页面

**依赖**：Task 4.2

**步骤**：
1. 创建 `pages/Login/index.tsx`
2. 使用 ProForm 实现表单
   - 密码输入框
   - 登录按钮
3. 创建 `services/api.ts`
   - 实现 `login()` API 调用
4. 登录成功后跳转到扫码页

**产物**：
- `ages/Login/index.tsx`
- `src/services/api.ts`

**验收标准**：
- [x] 页面样式符合 Ant Design 规范
- [x] 可以输入密码并登录
- [x] 登录成功跳转到扫码页
- [x] 登录失败显示错误提示

**预估时间**：1.5 小时

---

### Task 5.2: 实现微信扫码页面

**目标**：实现微信 QR 扫码页面

**依赖**：Task 4.2, Task 3.2

**步骤**：
1. 创建 `pages/WeixinQRCode/index.tsx`
2. 实现功能
   - 调用 `/api/weixin/qr` 获取二维码
   - 显示二维码图片
   - 轮询 `/api/weixin/qr/status` 检查状态
   - 显示状态文案（等待扫码/已扫码/已确认）
3. 扫码成功后跳转到聊天页

**产物**：
- `src/pages/WeixinQRCode/index.tsx`

**验收标准**：
- [x] 可以显示二维码
- [x] 状态变化正确显示
- [x] 扫码成功跳转到聊天页
- [x] 超时可以重新获取

**预估时间**：2 小时

---

### Task 5.3: 实现聊天页面

**目标**：实现聊天主界面

**依赖**：Task 4.3, Task 3.3

**步骤**：
1. 创建 `pages/Chat/index.tsx` 主布局
   - 左侧：会话列表
   - 右侧：消息区域 + 输入框
2. 创建 `pages/Chat/SessionList.tsx`
   - 使用 ProList 显示会话
   - 点击切换当前会话
3. 创建 `pages/Chat/MessageArea.tsx`
   - 显示消息气泡（左右对齐）
   - 滚动到最新消息
4. 创建 `pages/Chat/InputBox.tsx`
   - 文本输入框
   - 发送按钮
   - 回车发送
5. 集成 WebSocket
   - 连接建立后拉取历史消息
   - 实时接收新消息
   - 发送消息

**产物**：
- `src/pages/Chat/index.tsx`
- `src/pages/Chat/SessionList.tsx`
- `src/pages/Chat/MessageArea.tsx`
- `src/pages/Chat/InputBox.tsx`

**验收标准**：
- [x] 会话列表正常显示
- [x] 可以切换会话
- [x] 消息气泡样式正确（左右对齐）
- [x] 可以发送消息
- [x] 实时接收新消息
- [x] 刷新页面后可以看到历史消息

**预估时间**：4 小时

---

## Phase 6: 集成测试

### Task 6.1: 端到端测试

**目标**：完整测试整个流程

**依赖**：所有前置任务

**步骤**：
1. 启动后端服务
2. 启动前端服务
3. 测试流程
   - 密码登录
   - 微信扫码
   - 微信发送消息 → Web 接收
   - Web 发送消息 → 微信接收
   - 刷新页面 → 历史消息显示
   - 服务重启 → 重新连接
4. 修复发现的 Bug

**产物**：
- 测试报告

**验收标准**：
- [x] 所有功能正常工作
- [x] 无明显 Bug

**预估时间**：2 小时

---

### Task 6.2: 文档完善

**目标**：完善项目文档

**依赖**：Task 6.1

**步骤**：
1. 编写 README.md
   - 项目介绍
   - 功能特性
   - 快速开始
   - 配置说明
   - 常见问题
2. 编写部署文档
3. 添加代码注释

**产物**：
- `README.md`
- `DEPLOY.md`

**验收标准**：
- [x] 文档清晰易懂
- [x] 可以按照文档快速启动项目

**预估时间**：1.5 小时


## 总结

**总任务数**：18 个

**预估总时间**：约 30 小时

**关键路径**：
```
Task 1.1 → Task 1.2 → Task 2.1 → Task 2.3 → Task 2.4 → Task 3.2 → Task 5.2 → Task 5.3 → Task 6.1
```

**并行任务**：
- Task 2.2（消息缓存）可以和 Task 2.1 并行
- Task 3.1（认证接口）可以和 Task 2.x 并行
- Task 4.x（前端基础）可以和 Task 2.x/3.x 并行
- Task 5.1（登录页）可以和 Task 5.2 并行

**风险提示**：
1. 微信 iLink 协议可能需要调试（Task 2.1）
2. WebSocket 连接稳定性需要测试（Task 2.4）
3. 前端实时消息更新需要优化（Task 5.3）
