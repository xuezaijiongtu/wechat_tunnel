package com.xiaopeng.frd.wechat.dto;

import lombok.Data;

import java.util.List;

/**
 * getUpdates接口响应DTO
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class GetUpdatesResponse {

    /** 错误码 */
    private Integer errcode;

    /** 错误信息 */
    private String errmsg;

    /** 消息列表 */
    private List<WeixinMessageDTO> msgs;

    /** 下次轮询游标 */
    private String getUpdatesBuf;
}
