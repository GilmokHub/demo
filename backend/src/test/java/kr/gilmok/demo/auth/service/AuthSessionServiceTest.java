package kr.gilmok.demo.auth.service;

import kr.gilmok.demo.auth.entity.AuthSession;
import kr.gilmok.demo.user.entity.User;
import kr.gilmok.demo.auth.repository.AuthSessionRepository;
import kr.gilmok.demo.global.util.TokenHashEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthSessionServiceTest {

    @InjectMocks
    private AuthSessionService authSessionService;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authSessionService, "refreshExpTime", 604800000L); // 7 days in ms
        testUser = User.createNewUser("testuser", "password");
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Test
    @DisplayName("saveSession 호출 시 세션 저장을 위임하여 처리한다")
    void saveSession_delegatesSuccessfully() {
        // given
        String refreshToken = "refresh-token";
        String ip = "192.168.0.1";
        String userAgent = "Mozilla/5.0";

        // when & then: 미구현 상태(TDD Red)에서는 UnsupportedOperationException 발생 검증
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                authSessionService.saveSession(testUser, refreshToken, ip, userAgent))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("TDD Red");
    }
}
