package kr.gilmok.demo.auth.repository;

import jakarta.persistence.LockModeType;
import kr.gilmok.demo.auth.entity.AuthSession;
import kr.gilmok.demo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthSession> findByRefreshTokenHash(String refreshTokenHash);

    Optional<AuthSession> findByUser(User user);

    Optional<AuthSession> findByUserId(Long userId);

    default Optional<AuthSession> findByRefreshTokenHashAndActive(String hash) {
        return findByRefreshTokenHash(hash);
    }
}