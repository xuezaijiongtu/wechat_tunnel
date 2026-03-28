package com.xiaopeng.frd.wechat.config;

import com.xiaopeng.frd.wechat.service.MessageRouterService;
import com.xiaopeng.frd.wechat.service.WebSocketService;
import com.xiaopeng.frd.wechat.service.WeixinILinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 消息流初始化配置
 * <p>
 * 将WeixinILinkService的消息回调连接到MessageRouterService，
 * 将MessageRouterService的推送回调连接到WebSocketService
 * </p>
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Configuration
public class MessageFlowConfig {

    private final WeixinILinkService weixinILinkService;
    private final MessageRouterService messageRouterService;
    private final WebSocketService webSocketService;

    public MessageFlowConfig(WeixinILinkService weixinILinkService,
                             MessageRouterService messageRouterService,
                             WebSocketService webSocketService) {
        this.weixinILinkService = weixinILinkService;
        this.messageRouterService = messageRouterService;
        this.webSocketService = webSocketService;
    }

    @PostConstruct
    public void init() {
        // 微信消息 -> MessageRouterService
        weixinILinkService.setMessageCallback(messageRouterService::handleWeixinMessage);

        // MessageRouterService -> WebSocket广播
        messageRouterService.setPushCallback(webSocketService::broadcastMessage);

        log.info("消息流初始化完成: 微信 -> 路由 -> WebSocket");
    }
}
