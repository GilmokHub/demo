package kr.gilmok.demo.auth.repository;

import jakarta.persistence.LockModeType;
import kr.gilmok.demo.auth.entity.AuthSession;
import kr.gilmok.demo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    Optional<AuthSession> findByUser(User user);

    Optional<AuthSession> findByUserId(Long userId);

    default Optional<AuthSession> findByRefreshTokenHashAndActive(String hash) {
        return findByRefreshTokenHash(hash);
    }

    @Modifying
    @Query(value = """
            INSERT INTO auth_sessions (user_id, refresh_token_hash, created_at, expires_at, updated_at)
            VALUES (:userId, :tokenHash, :now, :expiresAt, :now)
            ON DUPLICATE KEY UPDATE
                refresh_token_hash = VALUES(refresh_token_hash),
                expires_at = VALUES(expires_at),
                updated_at = VALUES(updated_at)
            """, nativeQuery = true)
    void upsertAuthSession(
            @Param("userId") Long userId,
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now,
            @Param("expiresAt") LocalDateTime expiresAt);
}