package br.com.aquidolado.controller;

import br.com.aquidolado.dto.AuthResponse;
import br.com.aquidolado.dto.ForgotPasswordRequest;
import br.com.aquidolado.dto.LoginRequest;
import br.com.aquidolado.dto.RegisterRequest;
import br.com.aquidolado.dto.ResetPasswordRequest;
import br.com.aquidolado.dto.VerifyEmailRequest;
import br.com.aquidolado.service.AuthService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro, login, verificação de email e recuperação de senha")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário e retorna o token JWT. Envia email de verificação.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o usuário e retorna o token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Valida o token de verificação e marca o email como verificado")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar email de verificação", description = "Reenvia o email de verificação para o usuário autenticado")
    public ResponseEntity<Void> resendVerification() {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.resendVerificationEmail(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Esqueci minha senha", description = "Envia email com link para redefinir a senha (não revela se o email existe)")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha usando o token recebido por email")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
