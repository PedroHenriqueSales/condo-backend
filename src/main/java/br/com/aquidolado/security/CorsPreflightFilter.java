package br.com.aquidolado.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Responde ao preflight OPTIONS com headers CORS diretamente, garantindo que a resposta
 * venha da aplicação mesmo quando há proxy/CDN (ex.: Cloudflare/Render) na frente.
 * Registrado com maior precedência em CorsFilterConfig.
 */
public class CorsPreflightFilter extends OncePerRequestFilter {

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, Accept, Origin, X-Requested-With, Access-Control-Request-Method, Access-Control-Request-Headers";
    private static final long MAX_AGE = 3600L;

    private final Set<String> allowedOrigins;

    public CorsPreflightFilter(String allowedOriginsCsv, String extraOriginsCsv) {
        this.allowedOrigins = buildAllowedOrigins(allowedOriginsCsv, extraOriginsCsv);
    }

    private static Set<String> buildAllowedOrigins(String allowedOriginsCsv, String extraOriginsCsv) {
        Stream<String> main = parseCsv(allowedOriginsCsv);
        Stream<String> extra = parseCsv(extraOriginsCsv);
        return Stream.concat(main, extra)
                .filter(s -> !s.contains("*"))
                .collect(Collectors.toSet());
    }

    private static Stream<String> parseCsv(String csv) {
        if (!StringUtils.hasText(csv)) return Stream.empty();
        String normalized = csv.replace("%2C", ",");
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .filter(s -> !s.isEmpty());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");

        if (allowedOrigins.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean originAllowed = StringUtils.hasText(origin) && allowedOrigins.contains(origin);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            if (originAllowed) {
                response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                response.setHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(MAX_AGE));
            }
            response.setHeader("Cache-Control", "no-store, no-cache, max-age=0");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (originAllowed) {
            response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        }
        filterChain.doFilter(request, response);
    }
}
