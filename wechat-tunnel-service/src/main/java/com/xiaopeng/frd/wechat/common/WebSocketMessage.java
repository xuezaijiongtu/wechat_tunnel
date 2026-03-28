package com.xiaopeng.frd.wechat.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息封装
 *
 * @author system
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /** 消息类型: new_message / connection_status / send_message */
    private String type;

    /** 消息体 */
    private Object message;

    /** 连接状态（connection_status类型使用） */
    private Boolean connected;
}
