package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private static final String SENDGRID_API_URL = "https://api.sendgrid.com/v3/mail/send";

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

    @Value("${app.email.sendgrid-api-key:}")
    private String sendgridApiKey;

    @Autowired(required = false)
    private Environment environment;

    /**
     * Configurado quando há SMTP válido OU API Key do SendGrid (envio via HTTP, porta 443).
     * SendGrid API é útil no Render free tier, onde portas SMTP são bloqueadas.
     */
    public boolean isEmailConfigured() {
        if (!StringUtils.hasText(fromEmail) || !fromEmail.contains("@")) {
            return false;
        }
        if (StringUtils.hasText(sendgridApiKey)) {
            return true;
        }
        return mailSender != null 
                && StringUtils.hasText(smtpHost) 
                && StringUtils.hasText(smtpUsername);
    }

    private boolean useSendGridApi() {
        return StringUtils.hasText(sendgridApiKey) && StringUtils.hasText(fromEmail) && fromEmail.contains("@");
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
        if (!StringUtils.hasText(fromEmail) || !fromEmail.contains("@")) {
            log.error("❌ [EMAIL] EMAIL_FROM não configurado ou inválido. Configure a variável EMAIL_FROM com um email válido.");
            throw new IllegalStateException("EMAIL_FROM não está configurado. Configure a variável de ambiente EMAIL_FROM com um email válido.");
        }

        if (useSendGridApi()) {
            log.info("📧 [EMAIL] Modo de envio: SendGrid API (HTTPS). Enviando verificação para {}", user.getEmail());
            sendViaSendGridApi(user.getEmail(), user.getName(), "Confirme seu email - Aqui", buildVerificationEmailBody(user.getName(), verificationLink));
            log.info("📧 [EMAIL] Email de verificação enviado com sucesso para {} (SendGrid API)", user.getEmail());
            return;
        }

        log.info("📧 [EMAIL] Modo de envio: SMTP. Enviando verificação para {}", user.getEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Confirme seu email - Aqui");
            String html = buildVerificationEmailBody(user.getName(), verificationLink);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("📧 [EMAIL] Email de verificação enviado com sucesso para {}", user.getEmail());
        } catch (jakarta.mail.internet.AddressException e) {
            log.error("❌ [EMAIL] Email FROM inválido: {}. Verifique a variável EMAIL_FROM.", fromEmail);
            throw new IllegalStateException("EMAIL_FROM inválido: " + fromEmail + ". Configure um email válido na variável de ambiente EMAIL_FROM.", e);
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
        if (!StringUtils.hasText(fromEmail) || !fromEmail.contains("@")) {
            log.error("❌ [EMAIL] EMAIL_FROM não configurado ou inválido. Configure a variável EMAIL_FROM com um email válido.");
            throw new IllegalStateException("EMAIL_FROM não está configurado. Configure a variável de ambiente EMAIL_FROM com um email válido.");
        }

        if (useSendGridApi()) {
            log.info("📧 [EMAIL] Modo de envio: SendGrid API (HTTPS). Enviando reset de senha para {}", user.getEmail());
            sendViaSendGridApi(user.getEmail(), user.getName(), "Redefinição de senha - Aqui", buildPasswordResetEmailBody(user.getName(), resetLink));
            log.info("📧 [EMAIL] Email de redefinição de senha enviado com sucesso para {} (SendGrid API)", user.getEmail());
            return;
        }

        log.info("📧 [EMAIL] Modo de envio: SMTP. Enviando reset de senha para {}", user.getEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Redefinição de senha - Aqui");
            String html = buildPasswordResetEmailBody(user.getName(), resetLink);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("📧 [EMAIL] Email de redefinição de senha enviado com sucesso para {}", user.getEmail());
        } catch (jakarta.mail.internet.AddressException e) {
            log.error("❌ [EMAIL] Email FROM inválido: {}. Verifique a variável EMAIL_FROM.", fromEmail);
            throw new IllegalStateException("EMAIL_FROM inválido: " + fromEmail + ". Configure um email válido na variável de ambiente EMAIL_FROM.", e);
        } catch (MessagingException e) {
            log.error("❌ [EMAIL] Falha ao enviar email de reset para {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Não foi possível enviar o email de redefinição de senha", e);
        }
    }

    /**
     * Envia email via API HTTP do SendGrid (porta 443). Funciona em ambientes que bloqueiam SMTP (ex.: Render free tier).
     */
    private void sendViaSendGridApi(String toEmail, String toName, String subject, String htmlContent) {
        Map<String, Object> body = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail, "name", toName != null ? toName : "")))),
                "from", Map.of("email", fromEmail, "name", "Aqui"),
                "subject", subject,
                "content", List.of(Map.of("type", "text/html", "value", htmlContent))
        );

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(25));
        factory.setReadTimeout(Duration.ofSeconds(25));
        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .build();

        try {
            restClient.post()
                    .uri(SENDGRID_API_URL)
                    .header("Authorization", "Bearer " + sendgridApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("❌ [EMAIL] Falha na chamada à SendGrid API: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar email via SendGrid API: " + e.getMessage(), e);
        }
    }

    private String buildVerificationEmailBody(String name, String link) {
        return """
            <p>Olá, %s!</p>
            <p>Clique no link abaixo para confirmar seu email e ativar sua conta no Aqui:</p>
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
            <p>Recebemos uma solicitação para redefinir a senha da sua conta no Aqui.</p>
            <p><a href="%s" style="display:inline-block;padding:10px 20px;background:#2563eb;color:white;text-decoration:none;border-radius:6px;">Redefinir senha</a></p>
            <p>Ou copie e cole no navegador:</p>
            <p style="word-break:break-all;">%s</p>
            <p>Este link expira em 1 hora.</p>
            <p>Se você não solicitou a redefinição, ignore este email. Sua senha permanecerá inalterada.</p>
            """.formatted(name != null ? name : "usuário", link, link);
    }
}
