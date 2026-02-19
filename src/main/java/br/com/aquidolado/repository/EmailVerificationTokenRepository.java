package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByUser_Id(Long userId);

    void deleteByExpiresAtBefore(Instant instant);
}
