package kr.gilmok.demo.auth.service;

import kr.gilmok.demo.auth.entity.AuthSession;
import kr.gilmok.demo.user.entity.User;
import kr.gilmok.demo.auth.repository.AuthSessionRepository;
import kr.gilmok.demo.global.util.TokenHashEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;
    private final TokenHashEncoder tokenHashEncoder;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpTime;

    @Transactional
    public void saveSession(User user, String refreshToken, String ip, String userAgent) {
        createOrUpdateSession(user, refreshToken, ip, userAgent);
    }

    @Transactional
    public void createOrUpdateSession(User user, String refreshToken, String ip, String userAgent) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        String hashedRefreshToken = tokenHashEncoder.encode(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(refreshExpTime * 1_000_000L);

        AuthSession session = authSessionRepository.findByUser(user)
                .map(existing -> {
                    existing.updateRefreshToken(hashedRefreshToken, expiresAt);
                    return existing;
                })
                .orElseGet(() -> AuthSession.builder()
                        .user(user)
                        .refreshTokenHash(hashedRefreshToken)
                        .expiresAt(expiresAt)
                        .build());

        authSessionRepository.save(session);
        log.debug("단일 세션 저장/갱신 완료 (userId: {}, ip: {})", user.getId(), ip);
    }
}