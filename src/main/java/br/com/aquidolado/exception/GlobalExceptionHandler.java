package br.com.aquidolado.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Requisição rejeitada (IllegalArgumentException): {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(br.com.aquidolado.exception.AlreadyReportedException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyReported(br.com.aquidolado.exception.AlreadyReportedException ex) {
        log.warn("Denúncia duplicada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        log.warn("Requisição rejeitada (validação): {}", errors);
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuth(org.springframework.security.core.AuthenticationException ex) {
        log.warn("Falha de autenticação: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciais inválidas"));
    }

    /**
     * Trata exceções quando o cliente fecha a conexão antes do servidor terminar de responder.
     * Isso é comum quando o usuário navega para outra página ou cancela a requisição.
     * Não é um erro crítico, então apenas logamos em DEBUG.
     */
    @ExceptionHandler({AsyncRequestNotUsableException.class})
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        // Cliente cancelou a requisição - comportamento esperado, não logar como erro
        log.debug("Cliente cancelou a requisição: {}", ex.getMessage());
    }

    /**
     * Trata exceções de conexão abortada pelo cliente (ClientAbortException do Tomcat).
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void handleClientAbort(org.apache.catalina.connector.ClientAbortException ex) {
        // Cliente fechou a conexão - comportamento esperado, não logar como erro
        log.debug("Conexão fechada pelo cliente: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
        // Ignora exceções de conexão abortada pelo cliente (já tratadas acima)
        if (ex instanceof AsyncRequestNotUsableException || 
            ex instanceof org.apache.catalina.connector.ClientAbortException ||
            (ex.getCause() != null && ex.getCause() instanceof org.apache.catalina.connector.ClientAbortException)) {
            log.debug("Conexão fechada pelo cliente: {}", ex.getMessage());
            return null; // Não retorna resposta, conexão já foi fechada
        }
        
        log.error("Erro não tratado ao processar requisição: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocorreu um erro interno. Tente novamente mais tarde."));
    }
}
