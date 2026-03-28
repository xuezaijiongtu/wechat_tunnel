package com.xiaopeng.frd.wechat.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaopeng.frd.wechat.cache.ContextTokenCache;
import com.xiaopeng.frd.wechat.cache.InMemoryMessageCache;
import com.xiaopeng.frd.wechat.dto.WebMessageDTO;
import com.xiaopeng.frd.wechat.dto.WeixinMessageDTO;
import com.xiaopeng.frd.wechat.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息路由服务
 * <p>
 * 负责微信消息和Web消息之间的路由转发
 * </p>
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Service
public class MessageRouterService {

    private final InMemoryMessageCache messageCache;
    private final ContextTokenCache tokenCache;
    private final WeixinILinkService weixinService;

    /** WebSocket推送回调，由WebSocketService设置 */
    private java.util.function.Consumer<MessageVO> pushCallback;

    public MessageRouterService(InMemoryMessageCache messageCache,
                                ContextTokenCache tokenCache,
                                WeixinILinkService weixinService) {
        this.messageCache = messageCache;
        this.tokenCache = tokenCache;
        this.weixinService = weixinService;
    }

    /**
     * 设置WebSocket推送回调
     *
     * @param callback 推送回调
     */
    public void setPushCallback(java.util.function.Consumer<MessageVO> callback) {
        this.pushCallback = callback;
    }

    /**
     * 处理来自微信的消息
     *
     * @param msg 微信消息
     */
    public void handleWeixinMessage(WeixinMessageDTO msg) {
        String userId = msg.getFromUserId();

        // 打印完整的消息数据用于调试
        log.info("收到微信消息原始数据: {}", msg);
        log.info("itemList类型: {}, 内容: {}",
                msg.getItemList() != null ? msg.getItemList().getClass().getName() : "null",
                msg.getItemList());

        String text = extractText(msg);
        String contextToken = msg.getContextToken();

        log.info("收到微信消息, userId={}, text={}, contextToken={}", userId, text, contextToken);

        // 缓存context_token
        if (contextToken != null) {
            tokenCache.put(userId, contextToken);
        }

        // 构造消息VO
        MessageVO messageVO = MessageVO.builder()
                .userId(userId)
                .content(text)
                .direction("inbound")
                .timestamp(System.currentTimeMillis())
                .build();

        // 添加到缓存
        messageCache.addMessage(userId, messageVO);

        // 推送到Web
        if (pushCallback != null) {
            pushCallback.accept(messageVO);
        }
    }

    /**
     * 处理来自Web的消息
     *
     * @param msg Web消息
     */
    public void handleWebMessage(WebMessageDTO msg) {
        String userId = msg.getUserId();
        String text = msg.getContent();

        log.info("收到Web消息, userId={}, text={}", userId, text);

        // 获取context_token
        String contextToken = tokenCache.get(userId);
        if (contextToken == null) {
            log.warn("未找到contextToken, userId={}", userId);
            return;
        }

        // 构造消息VO
        MessageVO messageVO = MessageVO.builder()
                .userId(userId)
                .content(text)
                .direction("outbound")
                .timestamp(System.currentTimeMillis())
                .build();

        // 添加到缓存
        messageCache.addMessage(userId, messageVO);

        // 发送到微信
        try {
            weixinService.sendMessage(userId, text, contextToken);
        } catch (Exception e) {
            log.error("发送消息到微信失败, userId={}", userId, e);
        }

        // 回显到Web
        if (pushCallback != null) {
            pushCallback.accept(messageVO);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 从微信消息中提取文本内容
     */
    private String extractText(WeixinMessageDTO msg) {
        Object itemList = msg.getItemList();
        if (itemList instanceof JSONArray) {
            JSONArray items = (JSONArray) itemList;
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = items.getJSONObject(i);
                // 检查 text_item 字段（新的数据结构）
                JSONObject textItem = item.getJSONObject("text_item");
                if (textItem != null) {
                    String text = textItem.getString("text");
                    if (text != null && !text.isEmpty()) {
                        return text;
                    }
                }
                // 兼容旧的数据结构
                if ("text".equals(item.getString("content_type"))) {
                    return item.getString("content");
                }
            }
        }
        return "";
    }
}
