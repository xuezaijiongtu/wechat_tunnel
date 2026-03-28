package com.xiaopeng.frd.wechat.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息视图对象
 *
 * @author system
 * @date 2026-03-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    /** 用户ID */
    private String userId;

    /** 消息内容 */
    private String content;

    /** 消息方向: inbound-收到, outbound-发出 */
    private String direction;

    /** 时间戳 */
    private Long timestamp;
}
