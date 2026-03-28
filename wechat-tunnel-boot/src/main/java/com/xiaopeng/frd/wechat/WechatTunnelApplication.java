package com.xiaopeng.frd.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 微信消息隧道服务启动类
 *
 * @author system
 * @date 2026-03-28
 */
@EnableAsync
@SpringBootApplication
public class WechatTunnelApplication {

    public static void main(String[] args) {
        SpringApplication.run(WechatTunnelApplication.class, args);
    }
}
