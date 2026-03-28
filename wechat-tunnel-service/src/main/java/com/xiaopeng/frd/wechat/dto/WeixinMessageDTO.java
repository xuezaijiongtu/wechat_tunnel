package com.xiaopeng.frd.wechat.dto;

import lombok.Data;

/**
 * 微信消息DTO
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class WeixinMessageDTO {

    /** 发送者用户ID */
    private String fromUserId;

    /** 上下文Token（回复消息时必需） */
    private String contextToken;

    /** 消息项列表（JSON结构） */
    private Object itemList;

    /** 原始消息体 */
    private Object rawMessage;
}
