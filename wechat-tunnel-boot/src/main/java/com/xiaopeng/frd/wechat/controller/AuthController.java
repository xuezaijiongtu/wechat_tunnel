package com.xiaopeng.frd.wechat.controller;

import com.xiaopeng.frd.wechat.common.ResponseWrapper;
import com.xiaopeng.frd.wechat.dto.LoginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 认证管理接口
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${web.password:admin123}")
    private String webPassword;

    /**
     * 密码登录
     *
     * @param dto 登录参数
     * @return Session Token
     */
    @PostMapping("/login")
    public ResponseWrapper<String> login(@RequestBody LoginDTO dto) {
        try {
            if (dto == null || dto.getPassword() == null) {
                return ResponseWrapper.fail("400", "密码不能为空");
            }

            if (!webPassword.equals(dto.getPassword())) {
                log.warn("登录失败, 密码错误");
                return ResponseWrapper.fail("401", "密码错误");
            }

            String token = UUID.randomUUID().toString();
            log.info("登录成功, token={}", token);
            return ResponseWrapper.success(token);
        } catch (Exception e) {
            log.error("登录失败, param={}", dto, e);
            return ResponseWrapper.fail("500", "登录失败: " + e.getMessage());
        }
    }
}
