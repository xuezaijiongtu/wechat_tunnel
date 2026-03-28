package com.xiaopeng.frd.wechat.dto;

import lombok.Data;

/**
 * 二维码状态DTO
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class QRStatusDTO {

    /** 扫码状态: scanned / confirmed / expired */
    private String status;

    /** Bot Token（仅confirmed时返回） */
    private String botToken;
}
