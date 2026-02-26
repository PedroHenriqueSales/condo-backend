package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.EmailVerificationToken;
import br.com.aquidolado.domain.entity.PasswordResetToken;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.repository.EmailVerificationTokenRepository;
import br.com.aquidolado.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Environment environment;

    @Value("${app.email.verification-expiry-hours:24}")
    private int verificationExpiryHours;

    @Value("${app.email.reset-expiry-hours:1}")
    private int resetExpiryHours;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    @Transactional
    public String generateVerificationToken(User user) {
        verificationTokenRepository.deleteByUser_Id(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(verificationExpiryHours * 3600L);
        EmailVerificationToken entity = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();
        verificationTokenRepository.save(entity);
        log.info("🔑 [TOKEN] Token de verificação gerado - UserId: {}, Email: {}", user.getId(), user.getEmail());
        if (isDevProfile()) {
            log.debug("🔑 [TOKEN] Detalhes - Token: {}, Expira em: {}", token, expiresAt);
        }
        return token;
    }

    @Transactional
    public String generatePasswordResetToken(User user) {
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(resetExpiryHours * 3600L);
        PasswordResetToken entity = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(now)
                .build();
        passwordResetTokenRepository.save(entity);
        log.info("🔑 [TOKEN] Token de reset de senha gerado - UserId: {}, Email: {}", user.getId(), user.getEmail());
        if (isDevProfile()) {
            log.debug("🔑 [TOKEN] Detalhes - Token: {}, Expira em: {}", token, expiresAt);
        }
        return token;
    }

    @Transactional(readOnly = true)
    public User validateVerificationToken(String token) {
        log.debug("🔑 [TOKEN] Validando token de verificação");
        EmailVerificationToken evt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("🔑 [TOKEN] Token de verificação inválido");
                    return new IllegalArgumentException("Token de verificação inválido");
                });
        if (evt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenRepository.delete(evt);
            log.warn("🔑 [TOKEN] Token de verificação expirado - UserId: {}, Email: {}", 
                    evt.getUser().getId(), evt.getUser().getEmail());
            throw new IllegalArgumentException("Token de verificação expirado");
        }
        log.info("🔑 [TOKEN] Token de verificação válido - UserId: {}, Email: {}", evt.getUser().getId(), evt.getUser().getEmail());
        return evt.getUser();
    }

    @Transactional
    public User validateAndConsumePasswordResetToken(String token) {
        log.debug("🔑 [TOKEN] Validando token de reset de senha");
        PasswordResetToken prt = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> {
                    log.warn("🔑 [TOKEN] Token de reset inválido ou já utilizado");
                    return new IllegalArgumentException("Token de redefinição inválido ou já utilizado");
                });
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            prt.setUsed(true);
            passwordResetTokenRepository.save(prt);
            log.warn("🔑 [TOKEN] Token de reset expirado - UserId: {}, Email: {}", 
                    prt.getUser().getId(), prt.getUser().getEmail());
            throw new IllegalArgumentException("Token de redefinição expirado");
        }
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        log.info("🔑 [TOKEN] Token de reset válido e consumido - UserId: {}, Email: {}", prt.getUser().getId(), prt.getUser().getEmail());
        return prt.getUser();
    }

    @Transactional
    public void deleteVerificationTokenForUser(Long userId) {
        verificationTokenRepository.deleteByUser_Id(userId);
    }

    @Transactional
    public void deleteAllTokensForUser(Long userId) {
        verificationTokenRepository.deleteByUser_Id(userId);
        passwordResetTokenRepository.deleteByUser_Id(userId);
    }

    /**
     * Remove tokens de verificação de email expirados.
     */
    @Transactional
    public void cleanupExpiredVerificationTokens() {
        verificationTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }

    /**
     * Remove tokens de reset de senha expirados.
     */
    @Transactional
    public void cleanupExpiredPasswordResetTokens() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
