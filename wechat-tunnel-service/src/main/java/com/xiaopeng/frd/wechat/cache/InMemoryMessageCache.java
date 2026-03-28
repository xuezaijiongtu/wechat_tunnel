package com.xiaopeng.frd.wechat.cache;

import com.xiaopeng.frd.wechat.vo.MessageVO;
import com.xiaopeng.frd.wechat.vo.SessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * 内存消息缓存
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Component
public class InMemoryMessageCache {

    private static final int MAX_SIZE_PER_USER = 50;

    /** userId -> Queue<MessageVO> */
    private final Map<String, Queue<MessageVO>> cache = new ConcurrentHashMap<>();

    /**
     * 添加消息
     *
     * @param userId  用户ID
     * @param message 消息
     */
    public void addMessage(String userId, MessageVO message) {
        Queue<MessageVO> queue = cache.computeIfAbsent(
                userId,
                k -> new ConcurrentLinkedQueue<>()
        );

        queue.offer(message);

        // 保留最近50条
        while (queue.size() > MAX_SIZE_PER_USER) {
            queue.poll();
        }
    }

    /**
     * 获取用户的所有消息
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    public List<MessageVO> getMessages(String userId) {
        Queue<MessageVO> queue = cache.get(userId);
        return queue == null ? Collections.emptyList() : new ArrayList<>(queue);
    }

    /**
     * 获取所有会话
     *
     * @return 会话列表，按最后消息时间倒序
     */
    public List<SessionVO> getAllSessions() {
        return cache.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    Queue<MessageVO> messages = entry.getValue();
                    MessageVO lastMsg = null;
                    for (MessageVO msg : messages) {
                        lastMsg = msg;
                    }

                    return SessionVO.builder()
                            .userId(userId)
                            .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                            .lastMessageTime(lastMsg != null ? lastMsg.getTimestamp() : 0L)
                            .unreadCount(0)
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
