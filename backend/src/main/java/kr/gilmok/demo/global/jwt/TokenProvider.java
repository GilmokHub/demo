package kr.gilmok.demo.global.jwt;

import kr.gilmok.demo.auth.entity.User;

public interface TokenProvider {
    String createAccessToken(User user);

    String createRefreshToken(User user);
}
