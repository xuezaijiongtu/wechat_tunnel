package com.xiaopeng.frd.wechat.common;

import lombok.Data;

/**
 * 统一API响应封装
 * <p>
 * 替代 com.xiaopeng.frd.core.api.ResponseWrapper，
 * 内网环境可切换回原版
 * </p>
 *
 * @author system
 * @date 2026-03-28
 */
@Data
public class ResponseWrapper<T> {

    /** 是否成功 */
    private boolean success;

    /** 响应码 */
    private String code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    public static <T> ResponseWrapper<T> success() {
        ResponseWrapper<T> wrapper = new ResponseWrapper<>();
        wrapper.setSuccess(true);
        wrapper.setCode("200");
        wrapper.setMessage("success");
        return wrapper;
    }

    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> wrapper = new ResponseWrapper<>();
        wrapper.setSuccess(true);
        wrapper.setCode("200");
        wrapper.setMessage("success");
        wrapper.setData(data);
        return wrapper;
    }

    public static <T> ResponseWrapper<T> fail(String code, String message) {
        ResponseWrapper<T> wrapper = new ResponseWrapper<>();
        wrapper.setSuccess(false);
        wrapper.setCode(code);
        wrapper.setMessage(message);
        return wrapper;
    }
}
