package com.xiaopeng.frd.wechat.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文Token缓存
 * <p>
 * 缓存微信用户的context_token，回复消息时必需
 * </p>
 *
 * @author system
 * @date 2026-03-28
 */
@Slf4j
@Component
public class ContextTokenCache {

    /** userId -> contextToken */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 缓存Token
     *
     * @param userId       用户ID
     * @param contextToken 上下文Token
     */
    public void put(String userId, String contextToken) {
        cache.put(userId, contextToken);
        log.debug("缓存contextToken, userId={}", userId);
    }

    /**
     * 获取Token
     *
     * @param userId 用户ID
     * @return contextToken，不存在返回null
     */
    public String get(String userId) {
        return cache.get(userId);
    }
}
