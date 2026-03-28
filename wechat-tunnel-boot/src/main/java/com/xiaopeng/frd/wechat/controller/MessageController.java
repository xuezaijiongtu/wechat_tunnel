package com.xiaopeng.frd.wechat.controller;

import com.xiaopeng.frd.wechat.common.ResponseWrapper;
import com.xiaopeng.frd.wechat.cache.InMemoryMessageCache;
import com.xiaopeng.frd.wechat.vo.MessageVO;
import com.xiaopeng.frd.wechat.vo.SessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息查询接口
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class MessageController {

    private final InMemoryMessageCache messageCache;

    /**
     * 构造器注入
     */
    public MessageController(InMemoryMessageCache messageCache) {
        this.messageCache = messageCache;
    }

    /**
     * 获取所有会话列表
     *
     * @return 会话列表，按最后消息时间倒序
     */
    @GetMapping("/sessions")
    public ResponseWrapper<List<SessionVO>> sessions() {
        List<SessionVO> sessions = messageCache.getAllSessions();
        return ResponseWrapper.success(sessions);
    }

    /**
     * 获取指定用户的消息历史
     *
     * @param userId 用户ID
     * @return 消息列表
     */
    @GetMapping("/messages")
    public ResponseWrapper<List<MessageVO>> messages(@RequestParam String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseWrapper.fail("400", "userId不能为空");
        }
        List<MessageVO> messages = messageCache.getMessages(userId);
        return ResponseWrapper.success(messages);
    }
}
