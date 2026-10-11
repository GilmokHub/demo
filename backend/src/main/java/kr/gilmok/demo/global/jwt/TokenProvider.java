package kr.gilmok.demo.global.jwt;

import kr.gilmok.demo.user.entity.User;

public interface TokenProvider {
    String createAccessToken(User user);

    String createRefreshToken(User user);

    String getJti(String token);

    long getRemainingTtlMs(String token);
}
