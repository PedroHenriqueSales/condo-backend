package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.dto.*;
import br.com.aquidolado.repository.UserRepository;
import br.com.aquidolado.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EventLogService eventLogService;
    private final Environment environment;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (isDevProfile()) {
            log.info("🔐 [AUTH] Tentativa de registro - Email: {}", request.getEmail());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            if (isDevProfile()) {
                log.warn("🔐 [AUTH] Registro falhou - Email já cadastrado: {}", request.getEmail());
            }
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .whatsapp(request.getWhatsapp())
                .address(request.getAddress())
                .invitesRemaining(5)
                .active(true)
                .build();

        user = userRepository.save(user);

        eventLogService.log(EventType.REGISTER, user.getId(), null);

        String token = jwtService.generateToken(user.getEmail(), user.getId());
        
        if (isDevProfile()) {
            log.info("🔐 [AUTH] Registro bem-sucedido - UserId: {}, Email: {}, Nome: {}", 
                    user.getId(), user.getEmail(), user.getName());
        }

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        if (isDevProfile()) {
            log.info("🔐 [AUTH] Tentativa de login - Email: {}", request.getEmail());
        }

        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            eventLogService.log(EventType.LOGIN, user.getId(), null);

            String token = jwtService.generateToken(user.getEmail(), user.getId());
            
            if (isDevProfile()) {
                log.info("🔐 [AUTH] Login bem-sucedido - UserId: {}, Email: {}, Nome: {}", 
                        user.getId(), user.getEmail(), user.getName());
            }

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            if (isDevProfile()) {
                log.warn("🔐 [AUTH] Login falhou - Email: {}, Motivo: {}", 
                        request.getEmail(), e.getMessage());
            }
            throw e;
        }
    }

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}
