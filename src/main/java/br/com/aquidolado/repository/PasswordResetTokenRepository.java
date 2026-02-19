package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    void deleteByUser_Id(Long userId);

    void deleteByExpiresAtBefore(Instant instant);
}
