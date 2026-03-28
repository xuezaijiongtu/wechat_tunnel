package com.xiaopeng.frd.wechat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话视图对象
 *
 * @author system
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {

    /** 用户ID */
    private String userId;

    /** 最后一条消息内容 */
    private String lastMessage;

    /** 最后一条消息时间戳 */
    private Long lastMessageTime;

    /** 未读消息数 */
    private Integer unreadCount;
}
