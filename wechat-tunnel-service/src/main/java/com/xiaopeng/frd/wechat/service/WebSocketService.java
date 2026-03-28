package com.xiaopeng.frd.wechat.service;

import com.alibaba.fastjson2.JSON;
import com.xiaopeng.frd.wechat.common.WebSocketMessage;
import com.xiaopeng.frd.wechat.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理服务
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Service
public class WebSocketService {

    /** 在线会话集合 */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     *
     * @param session WebSocket会话
     */
    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket会话已添加, sessionId={}, 当前在线={}", session.getId(), sessions.size());
    }

    /**
     * 移除会话
     *
     * @param session WebSocket会话
     */
    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        log.info("WebSocket会话已移除, sessionId={}, 当前在线={}", session.getId(), sessions.size());
    }

    /**
     * 广播消息到所有在线会话
     *
     * @param messageVO 消息
     */
    public void broadcastMessage(MessageVO messageVO) {
        WebSocketMessage wsMsg = WebSocketMessage.builder()
                .type("new_message")
                .message(messageVO)
                .build();

        String payload = JSON.toJSONString(wsMsg);
        TextMessage textMessage = new TextMessage(payload);

        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.error("推送消息失败, sessionId={}", entry.getKey(), e);
                }
            }
        }
    }

    /**
     * 发送消息到指定会话
     *
     * @param session WebSocket会话
     * @param wsMsg   WebSocket消息
     */
    public void sendToSession(WebSocketSession session, WebSocketMessage wsMsg) {
        if (session.isOpen()) {
            try {
                String payload = JSON.toJSONString(wsMsg);
                synchronized (session) {
                    session.sendMessage(new TextMessage(payload));
                }
            } catch (IOException e) {
                log.error("发送消息失败, sessionId={}", session.getId(), e);
            }
        }
    }

    /**
     * 获取在线会话数
     *
     * @return 在线数
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}
