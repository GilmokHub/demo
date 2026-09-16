package kr.gilmok.auth.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.gilmok.auth.global.dto.ErrorResponse;
import kr.gilmok.auth.global.exception.GlobalErrorCode;
import kr.gilmok.auth.global.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenBlocklistFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of(
            "/error", "/actuator/health", "/actuator/prometheus",
            "/swagger-ui/**", "/v3/api-docs/**",
            "/auth/login", "/auth/signup", "/auth/reissue", "/auth/logout"
    );

    private final AccessTokenBlocklistRepository accessTokenBlocklistRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PATHS.stream().anyMatch(skip -> {
            if (skip.endsWith("/**")) {
                return path.startsWith(skip.substring(0, skip.length() - 3));
            }
            return path.equals(skip);
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = resolveAccessTokenFromCookie(request);
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jti = JwtUtils.extractJti(accessToken, secretKey);
            if (jti != null && accessTokenBlocklistRepository.isBlocked(jti)) {
                log.warn("[Blocklist] 로그아웃된 토큰 접근 차단 - jti: {}", jti);
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, GlobalErrorCode.ACCESS_TOKEN_BLOCKED);
                return;
            }
        } catch (io.jsonwebtoken.JwtException e) {
            log.debug("[Blocklist] JWT 파싱 실패 (만료/잘못된 토큰): {}", e.getMessage());
        } catch (Exception e) {
            log.error("[Blocklist] 보안 시스템 장애 - 요청을 거부합니다. Error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, GlobalErrorCode.SECURITY_SYSTEM_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void sendErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
