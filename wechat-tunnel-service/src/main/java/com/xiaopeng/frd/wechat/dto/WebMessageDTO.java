package com.xiaopeng.frd.wechat.dto;

import lombok.Data;

/**
 * Web端消息DTO
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class WebMessageDTO {

    /** 目标用户ID */
    private String userId;

    /** 消息内容 */
    private String content;
}
