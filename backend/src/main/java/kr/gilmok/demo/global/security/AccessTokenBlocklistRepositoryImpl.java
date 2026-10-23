package kr.gilmok.demo.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AccessTokenBlocklistRepositoryImpl implements AccessTokenBlocklistRepository {

    private static final String KEY_PREFIX = "blocklist:jti:";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void block(String jti, long ttlMs) {
        if (ttlMs <= 0) {
            log.warn("[Blocklist] skip block registration: invalid ttlMs={}, jti={}", ttlMs, jti);
            return;
        }

        redisTemplate.opsForValue().set(
                buildKey(jti),
                "1",
                ttlMs,
                TimeUnit.MILLISECONDS
        );
    }

    private String buildKey(String jti) {
        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException("jti must not be null or blank");
        }
        return KEY_PREFIX + jti;
    }

    @Override
    public boolean isBlocked(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(jti)));
    }
}
