package kr.gilmok.demo.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdmissionTokenInterceptor implements HandlerInterceptor {

    public static final String ADMISSION_HEADER = "X-Admission-Token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder admissionJwtDecoder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 프리플라이트 및 GET 조회 요청은 토큰 검증 바이패스
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())
                || HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(ADMISSION_HEADER);
        if (header == null || header.trim().isEmpty()) {
            log.warn("[Admission Guard] 입장 토큰 누락 - URI: {}", request.getRequestURI());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "X-Admission-Token 헤더가 필요합니다.");
            return false;
        }

        String token = header.startsWith(BEARER_PREFIX) ? header.substring(BEARER_PREFIX.length()).trim() : header.trim();

        try {
            // 캐시된 JWKS 기반 RS256 서명 및 만료시간 오프라인 검증 (0ms)
            Jwt jwt = admissionJwtDecoder.decode(token);
            request.setAttribute("admissionJwt", jwt);
            return true;
        } catch (Exception e) {
            log.warn("[Admission Guard] 토큰 검증 실패: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않거나 만료된 입장 토큰입니다.");
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = String.format("{\"status\":%d,\"message\":\"%s\"}", status, message);
        response.getWriter().write(body);
    }
}
