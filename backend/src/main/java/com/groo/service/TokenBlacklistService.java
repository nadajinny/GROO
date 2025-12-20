package com.groo.service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TokenBlacklistService {

    private static final String PREFIX = "auth:blacklist:";
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String token, Duration ttl) {
        if (!StringUtils.hasText(token) || ttl == null) {
            return;
        }
        long seconds = ttl.getSeconds();
        if (seconds <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(PREFIX + token, "1", seconds, TimeUnit.SECONDS);
        } catch (DataAccessException ex) {
            log.warn("Failed to blacklist token due to Redis error: {}", ex.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (DataAccessException ex) {
            log.warn("Failed to check token blacklist due to Redis error: {}", ex.getMessage());
            return false;
        }
    }
}
