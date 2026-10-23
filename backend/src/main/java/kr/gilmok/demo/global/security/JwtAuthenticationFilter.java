package kr.gilmok.demo.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.gilmok.demo.global.dto.AuthUserDto;
import kr.gilmok.demo.global.dto.ErrorResponse;
import kr.gilmok.demo.global.exception.CustomException;
import kr.gilmok.demo.global.exception.GlobalErrorCode;
import kr.gilmok.demo.global.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        String token = resolveToken(request);

        // 이미 인증이 있으면 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (token == null) {
                log.debug("JWT 없음: {} {}", method, uri);
                meterRegistry.counter("token.validation", "result", "missing").increment();
                filterChain.doFilter(request, response);
                return;
            }

            boolean valid = JwtUtils.validateToken(token, secretKey);
            log.debug("JWT 존재: {} {} valid={}", method, uri, valid);

            if (!valid) {
                meterRegistry.counter("token.validation", "result", "invalid").increment();
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("SecurityContext 인증 세팅: name={}, uri={}", authentication.getName(), uri);

            meterRegistry.counter("token.validation", "result", "success").increment();

        } catch (CustomException e) {
            log.error("JWT CustomException: uri={} code={} msg={}", uri, e.getErrorCode().getCode(), e.getMessage());
            SecurityContextHolder.clearContext();
            meterRegistry.counter("token.validation", "result", "error").increment();
            sendErrorResponse(response, e);
            return;

        } catch (Exception e) {
            log.error("JWT 인증 에러: uri={} msg={}", uri, e.getMessage(), e);
            SecurityContextHolder.clearContext();
            meterRegistry.counter("token.validation", "result", "error").increment();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    return (value == null || value.trim().isEmpty()) ? null : value;
                }
            }
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        Claims claims = JwtUtils.extractClaims(token, secretKey);
        Long id = claims.get("id", Long.class);
        String username = claims.getSubject();
        String status = claims.get("status", String.class);
        String role = claims.get("role", String.class);

        if (status == null || role == null || role.isBlank()) {
            throw new CustomException(GlobalErrorCode.INVALID_USER);
        }

        if (!"ACTIVE".equals(status)) {
            throw new CustomException(GlobalErrorCode.INACTIVATED_USER);
        }

        AuthUserDto authUserDto = new AuthUserDto(
                id,
                username,
                "",
                role,
                status
        );

        CustomUserDetails principal = new CustomUserDetails(authUserDto);

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    private void sendErrorResponse(HttpServletResponse response, CustomException e) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(e.getErrorCode().getHttpStatus().value());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode());
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
