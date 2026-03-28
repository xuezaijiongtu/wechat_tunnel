package com.xiaopeng.frd.wechat.controller;

import com.alibaba.fastjson2.JSON;
import com.xiaopeng.frd.wechat.cache.InMemoryMessageCache;
import com.xiaopeng.frd.wechat.common.WebSocketMessage;
import com.xiaopeng.frd.wechat.dto.WebMessageDTO;
import com.xiaopeng.frd.wechat.service.MessageRouterService;
import com.xiaopeng.frd.wechat.service.WebSocketService;
import com.xiaopeng.frd.wechat.vo.MessageVO;
import com.xiaopeng.frd.wechat.vo.SessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

/**
 * WebSocket消息处理器
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;
    private final MessageRouterService messageRouter;
    private final InMemoryMessageCache messageCache;

    public WebSocketHandler(WebSocketService webSocketService,
                            MessageRouterService messageRouter,
                            InMemoryMessageCache messageCache) {
        this.webSocketService = webSocketService;
        this.messageRouter = messageRouter;
        this.messageCache = messageCache;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立, sessionId={}", session.getId());
        webSocketService.addSession(session);
        sendHistoryMessages(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息, sessionId={}, payload={}", session.getId(), payload);

        try {
            WebMessageDTO webMsg = JSON.parseObject(payload, WebMessageDTO.class);
            messageRouter.handleWebMessage(webMsg);
        } catch (Exception e) {
            log.error("处理WebSocket消息失败, sessionId={}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接关闭, sessionId={}, status={}", session.getId(), status);
        webSocketService.removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误, sessionId={}", session.getId(), exception);
        webSocketService.removeSession(session);
    }

    /**
     * 发送历史消息到新连接的会话
     */
    private void sendHistoryMessages(WebSocketSession session) {
        List<SessionVO> sessions = messageCache.getAllSessions();
        for (SessionVO sessionVO : sessions) {
            List<MessageVO> messages = messageCache.getMessages(sessionVO.getUserId());
            for (MessageVO msg : messages) {
                WebSocketMessage wsMsg = WebSocketMessage.builder()
                        .type("new_message")
                        .message(msg)
                        .build();
                webSocketService.sendToSession(session, wsMsg);
            }
        }
    }
}
