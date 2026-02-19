package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${app.email.from:}")
    private String fromEmail;

    @Value("${app.email.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Autowired(required = false)
    private Environment environment;

    public boolean isEmailConfigured() {
        return mailSender != null && StringUtils.hasText(smtpHost) && StringUtils.hasText(smtpUsername);
    }

    private boolean isDevProfile() {
        try {
            return environment != null && Arrays.asList(environment.getActiveProfiles()).contains("dev");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Envia email de verificação de conta. Em modo mock (SMTP não configurado), apenas loga o token.
     */
    public void sendVerificationEmail(User user, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        if (!isEmailConfigured()) {
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.warn("📧 [EMAIL MOCK] Email não configurado - modo mock");
            log.warn("📧 [EMAIL MOCK] Link de verificação para: {}", user.getEmail());
            if (isDevProfile()) {
                log.warn("📧 [EMAIL MOCK] Token: {}", token);
            }
            log.warn("📧 [EMAIL MOCK] Link completo: {}", verificationLink);
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Confirme seu email - Aquidolado");
            String html = buildVerificationEmailBody(user.getName(), verificationLink);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("📧 [EMAIL] Email de verificação enviado com sucesso para {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("❌ [EMAIL] Falha ao enviar email de verificação para {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o email de verificação", e);
        }
    }

    /**
     * Envia email de redefinição de senha. Em modo mock, apenas loga o token.
     */
    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        if (!isEmailConfigured()) {
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.warn("📧 [EMAIL MOCK] Email não configurado - modo mock");
            log.warn("📧 [EMAIL MOCK] Link de reset de senha para: {}", user.getEmail());
            if (isDevProfile()) {
                log.warn("📧 [EMAIL MOCK] Token: {}", token);
            }
            log.warn("📧 [EMAIL MOCK] Link completo: {}", resetLink);
            log.warn("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Redefinição de senha - Aquidolado");
            String html = buildPasswordResetEmailBody(user.getName(), resetLink);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("📧 [EMAIL] Email de redefinição de senha enviado com sucesso para {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("❌ [EMAIL] Falha ao enviar email de reset para {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o email de redefinição de senha", e);
        }
    }

    private String buildVerificationEmailBody(String name, String link) {
        return """
            <p>Olá, %s!</p>
            <p>Clique no link abaixo para confirmar seu email e ativar sua conta no Aquidolado:</p>
            <p><a href="%s" style="display:inline-block;padding:10px 20px;background:#2563eb;color:white;text-decoration:none;border-radius:6px;">Confirmar email</a></p>
            <p>Ou copie e cole no navegador:</p>
            <p style="word-break:break-all;">%s</p>
            <p>Este link expira em 24 horas.</p>
            <p>Se você não criou uma conta, ignore este email.</p>
            """.formatted(name != null ? name : "usuário", link, link);
    }

    private String buildPasswordResetEmailBody(String name, String link) {
        return """
            <p>Olá, %s!</p>
            <p>Recebemos uma solicitação para redefinir a senha da sua conta no Aquidolado.</p>
            <p><a href="%s" style="display:inline-block;padding:10px 20px;background:#2563eb;color:white;text-decoration:none;border-radius:6px;">Redefinir senha</a></p>
            <p>Ou copie e cole no navegador:</p>
            <p style="word-break:break-all;">%s</p>
            <p>Este link expira em 1 hora.</p>
            <p>Se você não solicitou a redefinição, ignore este email. Sua senha permanecerá inalterada.</p>
            """.formatted(name != null ? name : "usuário", link, link);
    }
}
