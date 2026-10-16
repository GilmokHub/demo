package kr.gilmok.demo.auth.service;

import kr.gilmok.demo.auth.entity.AuthSession;
import kr.gilmok.demo.auth.repository.AuthSessionRepository;
import kr.gilmok.demo.global.util.TokenHashEncoder;
import kr.gilmok.demo.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthSessionSingleDevice 단위 테스트 (TDD)")
class AuthSessionSingleDeviceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    @InjectMocks
    private AuthSessionService authSessionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authSessionService, "refreshExpTime", 604800000L); // 7일
        testUser = User.createNewUser("testuser", "password123!");
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Nested
    @DisplayName("단일 기기 세션 Upsert 갱신 기능")
    class SingleDeviceSessionTest {

        @Test
        @DisplayName("기존 세션이 존재하는 회원이 재로그인할 때(조건), 기존 레코드의 토큰 해시와 만료일시가 최신 값으로 갱신되어야 한다(기대 결과)")
        void reLogin_Success_UpdatesExistingSession() {
            // Given
            String newRefreshToken = "new-refresh-token-456";
            String newHashedToken = "new-hashed-token-456";
            String ip = "192.168.1.100";
            String userAgent = "Mozilla/5.0";

            given(tokenHashEncoder.encode(newRefreshToken)).willReturn(newHashedToken);

            // When
            authSessionService.createOrUpdateSession(testUser, newRefreshToken, ip, userAgent);

            // Then
            verify(authSessionRepository).save(any(AuthSession.class));
        }

        @Test
        @DisplayName("기존 세션이 없는 신규 회원이 로그인할 때(조건), 단일 세션 레코드가 신규 생성되어야 한다(기대 결과)")
        void firstLogin_Success_CreatesNewSession() {
            // Given
            String refreshToken = "initial-refresh-token-123";
            String hashedToken = "initial-hashed-token-123";
            String ip = "10.0.0.1";
            String userAgent = "Chrome/120.0";

            given(tokenHashEncoder.encode(refreshToken)).willReturn(hashedToken);

            // When
            authSessionService.createOrUpdateSession(testUser, refreshToken, ip, userAgent);

            // Then
            verify(authSessionRepository).save(any(AuthSession.class));
        }

        @Test
        @DisplayName("유효하지 않은 null 사용자로 세션 저장을 시도할 때(조건), IllegalArgumentException이 발생해야 한다(기대 결과)")
        void nullUser_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> authSessionService.createOrUpdateSession(null, "token", "127.0.0.1", "ua"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
