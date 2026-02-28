package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.dto.AuthResponse;
import br.com.aquidolado.dto.LoginRequest;
import br.com.aquidolado.dto.RegisterRequest;
import br.com.aquidolado.repository.UserRepository;
import br.com.aquidolado.security.JwtService;
import br.com.aquidolado.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EventLogService eventLogService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final Environment environment;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("🔐 [AUTH] Tentativa de registro - Email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("🔐 [AUTH] Registro falhou - Email já cadastrado: {}", request.getEmail());
            throw new IllegalArgumentException("Email já cadastrado");
        }

        String whatsapp = request.getWhatsapp() != null && !request.getWhatsapp().isBlank()
                ? PhoneUtil.normalize(request.getWhatsapp())
                : null;
        Instant now = Instant.now();
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .whatsapp(whatsapp)
                .address(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress().trim() : null)
                .invitesRemaining(5)
                .active(true)
                .emailVerified(false)
                .termsAcceptedAt(now)
                .privacyAcceptedAt(now)
                .build();

        user = userRepository.save(user);

        eventLogService.log(EventType.REGISTER, user.getId(), null);

        String verificationToken = tokenService.generateVerificationToken(user);
        try {
            emailService.sendVerificationEmail(user, verificationToken);
        } catch (Exception e) {
            log.warn("🔐 [AUTH] Falha ao enviar email de verificação (conta criada): {}", e.getMessage());
        }

        String token = jwtService.generateToken(user.getEmail(), user.getId());

        log.info("🔐 [AUTH] Registro bem-sucedido - UserId: {}, Email: {}, Nome: {}",
                user.getId(), user.getEmail(), user.getName());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .emailVerified(false)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("🔐 [AUTH] Tentativa de login - Email: {}", request.getEmail());

        try {

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            eventLogService.log(EventType.LOGIN, user.getId(), null);

            String token = jwtService.generateToken(user.getEmail(), user.getId());

            log.info("🔐 [AUTH] Login bem-sucedido - UserId: {}, Email: {}, Nome: {}",
                    user.getId(), user.getEmail(), user.getName());

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("🔐 [AUTH] Login falhou - Email: {}, Motivo: {}",
                    request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("✅ [AUTH] Verificando email");
        User fromToken = tokenService.validateVerificationToken(token);
        User user = userRepository.findById(fromToken.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenService.deleteVerificationTokenForUser(user.getId());
        log.info("✅ [AUTH] Email verificado com sucesso - UserId: {}, Email: {}", user.getId(), user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email já verificado");
        }
        String verificationToken = tokenService.generateVerificationToken(user);
        try {
            emailService.sendVerificationEmail(user, verificationToken);
            log.info("🔐 [AUTH] Email de verificação reenviado - UserId: {}, Email: {}", user.getId(), user.getEmail());
        } catch (Exception e) {
            log.warn("🔐 [AUTH] Falha ao reenviar email de verificação: {}", e.getMessage());
            throw new RuntimeException("Não foi possível reenviar o email de verificação", e);
        }
    }

    @Transactional
    public void forgotPassword(String email) {
        log.info("🔐 [AUTH] Solicitação de reset de senha - Email: {}", email);
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = tokenService.generatePasswordResetToken(user);
            try {
                emailService.sendPasswordResetEmail(user, resetToken);
                log.info("🔐 [AUTH] Email de reset enviado - UserId: {}, Email: {}", user.getId(), user.getEmail());
            } catch (IllegalStateException e) {
                // Erro de configuração de email (EMAIL_FROM inválido) - loga mas não quebra o fluxo
                log.error("🔐 [AUTH] Configuração de email inválida. Verifique EMAIL_FROM. Erro: {}", e.getMessage());
            } catch (Exception e) {
                log.warn("🔐 [AUTH] Falha ao enviar email de reset para {}: {}", user.getEmail(), e.getMessage());
            }
        });
        log.debug("🔐 [AUTH] Processamento de forgot-password concluído");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("🔐 [AUTH] Redefinindo senha");
        User user = tokenService.validateAndConsumePasswordResetToken(token);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("🔐 [AUTH] Senha redefinida com sucesso - UserId: {}, Email: {}", user.getId(), user.getEmail());
    }

}
