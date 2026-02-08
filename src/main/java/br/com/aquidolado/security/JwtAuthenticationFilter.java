package br.com.aquidolado.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final Environment environment;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (isDevProfile()) {
            String uri = request.getRequestURI();
            if (!uri.startsWith("/api/auth") && !uri.startsWith("/swagger") 
                    && !uri.startsWith("/v3/api-docs") && !uri.startsWith("/actuator")) {
                String authHeader = request.getHeader("Authorization");
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                StringBuilder headers = new StringBuilder();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (headerName.equalsIgnoreCase("Authorization")) {
                        headers.append(headerName).append(": ").append(authHeader != null ? (authHeader.length() > 30 ? authHeader.substring(0, 30) + "..." : authHeader) : "null").append("; ");
                    }
                }
                log.warn("🔐 [JWT] Headers recebidos - Authorization: {}, Todos os headers: {}", 
                        authHeader != null ? (authHeader.length() > 30 ? authHeader.substring(0, 30) + "..." : authHeader) : "null",
                        headers.toString());
            }
        }

        if (StringUtils.hasText(token)) {
            if (jwtService.isValid(token)) {
                String email = jwtService.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    if (isDevProfile()) {
                        log.info("🔐 [JWT] Token válido - Email: {}, Endpoint: {} {}", 
                                email, request.getMethod(), request.getRequestURI());
                    }
                }
            } else {
                if (isDevProfile()) {
                    log.warn("🔐 [JWT] Token inválido ou expirado - Endpoint: {} {}", 
                            request.getMethod(), request.getRequestURI());
                }
            }
        } else {
            // Não loga endpoints públicos para evitar poluição de logs
            String uri = request.getRequestURI();
            if (isDevProfile() && !uri.startsWith("/api/auth") && !uri.startsWith("/swagger") 
                    && !uri.startsWith("/v3/api-docs") && !uri.startsWith("/actuator")) {
                log.warn("🔐 [JWT] Requisição sem token - Endpoint: {} {}", 
                        request.getMethod(), uri);
            }
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        
        if (bearer == null || bearer.isBlank()) {
            return null;
        }
        
        // Remove espaços extras e verifica se começa com "Bearer"
        bearer = bearer.trim();
        
        if (bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7).trim();
            if (!token.isEmpty()) {
                return token;
            }
        } else if (bearer.startsWith("bearer ")) {
            // Case insensitive
            String token = bearer.substring(7).trim();
            if (!token.isEmpty()) {
                return token;
            }
        } else if (!bearer.contains(" ")) {
            // Se não tem "Bearer", assume que é só o token
            return bearer;
        }
        
        return null;
    }
}
