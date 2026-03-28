package com.xiaopeng.frd.wechat.dto;

import lombok.Data;

/**
 * 二维码DTO
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class QRCodeDTO {

    /** 二维码字符串 */
    private String qrcode;

    /** 二维码图片（Base64） */
    private String qrcodeImage;
}
