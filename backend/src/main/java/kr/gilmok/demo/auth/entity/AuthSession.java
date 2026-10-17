package kr.gilmok.demo.auth.entity;

import jakarta.persistence.*;
import kr.gilmok.demo.auth.exception.AuthErrorCode;
import kr.gilmok.demo.user.entity.User;
import kr.gilmok.demo.global.exception.CustomException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_sessions", indexes = {
        @Index(name = "idx_auth_session_hash", columnList = "refresh_token_hash")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_auth_sessions_user_id", columnNames = {"user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public AuthSession(User user, String refreshTokenHash, LocalDateTime expiresAt) {
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshTokenHash, LocalDateTime expiresAt) {
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void revoke() {
        this.expiresAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void validateExpiration(LocalDateTime now) {
        if (!this.expiresAt.isAfter(now)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    public boolean isExpired(LocalDateTime now) {
        return !this.expiresAt.isAfter(now);
    }
}