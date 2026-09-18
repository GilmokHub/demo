package kr.gilmok.demo.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.gilmok.demo.global.exception.CustomException;
import kr.gilmok.demo.global.exception.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
public class JwtUtils {

    public static boolean validateToken(String token, String secretKey) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다.");
        }
        return false;
    }

    public static Claims extractClaims(String token, String secretKey) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractJti(String token, String secretKey) {
        return extractClaims(token, secretKey).getId();
    }

    public static long getRemainingTtlMs(String token, String secretKey) {
        Date expiration = extractClaims(token, secretKey).getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    private static Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new CustomException(GlobalErrorCode.NOT_ENOUGH_KEY_LENGTH);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
