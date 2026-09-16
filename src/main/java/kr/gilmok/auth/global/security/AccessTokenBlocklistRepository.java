package kr.gilmok.auth.global.security;

public interface AccessTokenBlocklistRepository {

    default void block(String jti, long ttlMs) {
        throw new UnsupportedOperationException("이 모듈에서는 블랙리스트 등록 기능이 지원되지 않습니다.");
    }

    boolean isBlocked(String jti);
}
