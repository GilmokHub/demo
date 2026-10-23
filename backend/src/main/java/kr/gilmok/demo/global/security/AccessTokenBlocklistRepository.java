package kr.gilmok.demo.global.security;

public interface AccessTokenBlocklistRepository {

    void block(String jti, long ttlMs);

    boolean isBlocked(String jti);
}
