package br.com.aquidolado.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS deve ser o primeiro para permitir preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                        "/api/auth/verify-email", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS para deploy com frontend em domínio separado.
     * No dev, como usamos proxy do Vite, normalmente isso não é necessário.
     * Configure via env/props:
     * app.cors.allowed-origins=https://app.aquidolado.com,https://staging.aquidolado.com
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String allowedOriginsCsv,
            @Value("${app.cors.extra-origins:}") String extraOriginsCsv) {
        CorsConfiguration cfg = new CorsConfiguration();

        List<String> patterns = new ArrayList<>();

        if (StringUtils.hasText(allowedOriginsCsv)) {
            List<String> origins = Arrays.stream(allowedOriginsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            
            // Usa patterns para suportar wildcards (ex: URLs do Vercel)
            // Adiciona padrão do Vercel automaticamente se detectar URL do Vercel
            patterns = new ArrayList<>(origins);
            
            // Se houver alguma URL do Vercel, adiciona o padrão wildcard
            boolean hasVercelUrl = origins.stream().anyMatch(origin -> origin.contains(".vercel.app"));
            if (hasVercelUrl && !patterns.contains("https://*.vercel.app")) {
                patterns.add("https://*.vercel.app");
            }
        } else {
            // Dev: libera o Vite dev server e dispositivos na rede local (ex.: iPhone)
            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
                patterns = new ArrayList<>(List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://192.168.*:5173",
                        "http://10.*:5173",
                        "http://172.16.*:5173",
                        "http://172.17.*:5173",
                        "http://172.18.*:5173",
                        "http://172.19.*:5173",
                        "http://172.2*:5173",
                        "http://172.30.*:5173",
                        "http://172.31.*:5173"
                ));
            }
        }

        // Sempre permite chamadas a partir de apps Capacitor (WebView)
        // Android: "http://localhost" ou "http://localhost:PORT" (com ou sem barra final)
        // iOS: "capacitor://localhost"; alguns clientes enviam "null"
        List<String> capacitorOrigins = List.of(
                "capacitor://localhost",
                "capacitor://localhost/",
                "http://localhost",
                "http://localhost/",
                "http://localhost:*",
                "http://localhost:*/",
                "http://127.0.0.1",
                "http://127.0.0.1/",
                "http://127.0.0.1:*",
                "https://localhost",
                "https://localhost/",
                "https://localhost:*",
                "https://127.0.0.1",
                "https://127.0.0.1:*",
                "null"
        );
        for (String origin : capacitorOrigins) {
            if (!patterns.contains(origin)) {
                patterns.add(origin);
            }
        }

        // Origens extras (ex.: Origin exata do app no emulador, para depuração)
        if (StringUtils.hasText(extraOriginsCsv)) {
            List<String> finalPatterns = patterns;
            Arrays.stream(extraOriginsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(origin -> {
                        if (!finalPatterns.contains(origin)) finalPatterns.add(origin);
                    });
        }

        cfg.setAllowedOriginPatterns(patterns);

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
