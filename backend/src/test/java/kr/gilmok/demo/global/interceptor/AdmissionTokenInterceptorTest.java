package kr.gilmok.demo.global.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdmissionTokenInterceptor 단위 테스트")
class AdmissionTokenInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private AdmissionTokenInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private static final String VALID_TOKEN = "valid.admission.token";

    @BeforeEach
    void setUp() {
        interceptor = new AdmissionTokenInterceptor(jwtDecoder);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI("/reservations");
    }

    @Test
    @DisplayName("유효한 X-Admission-Token 헤더가 있으면 통과(true) 및 속성 설정")
    void preHandle_validToken_returnsTrue() throws Exception {
        // given
        request.addHeader("X-Admission-Token", "Bearer " + VALID_TOKEN);
        Jwt mockJwt = new Jwt(
                VALID_TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of("sub", "user123", "status", "ADMITTED", "evt", "event-1")
        );
        given(jwtDecoder.decode(VALID_TOKEN)).willReturn(mockJwt);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
        assertThat(request.getAttribute("admissionJwt")).isEqualTo(mockJwt);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    @DisplayName("X-Admission-Token 헤더가 누락되면 401 차단 및 false 반환")
    void preHandle_missingHeader_returnsFalseAnd401() throws Exception {
        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("X-Admission-Token 헤더가 필요합니다");
    }

    @Test
    @DisplayName("위조되거나 만료된 토큰인 경우 401 차단 및 false 반환")
    void preHandle_invalidToken_returnsFalseAnd401() throws Exception {
        // given
        request.addHeader("X-Admission-Token", "Bearer invalid-token");
        given(jwtDecoder.decode("invalid-token")).willThrow(new BadJwtException("Invalid signature"));

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("유효하지 않거나 만료된 입장 토큰입니다");
    }

    @Test
    @DisplayName("CORS OPTIONS 프리플라이트 요청은 토큰 없이도 통과")
    void preHandle_optionsMethod_returnsTrue() throws Exception {
        // given
        request.setMethod("OPTIONS");

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("GET 조회 요청은 대기열 토큰 없이도 통과")
    void preHandle_getMethod_returnsTrue() throws Exception {
        // given
        request.setMethod("GET");

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }
}
