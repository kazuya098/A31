package com.competition.competition.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存存储：token -> userId，用于登录态校验。演示/单机可用；集群需改为 Redis 等。
 */
@Component
public class AuthTokenStore {
    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String put(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        return token == null ? null : tokenToUserId.get(token);
    }

    public void remove(String token) {
        if (token != null) tokenToUserId.remove(token);
    }
}
