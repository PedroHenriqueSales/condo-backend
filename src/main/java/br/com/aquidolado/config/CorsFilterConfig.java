package br.com.aquidolado.config;

import br.com.aquidolado.security.CorsPreflightFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class CorsFilterConfig {

    @Bean
    public CorsPreflightFilter corsPreflightFilter(
            @Value("${app.cors.allowed-origins:}") String allowedOriginsCsv,
            @Value("${app.cors.extra-origins:}") String extraOriginsCsv) {
        return new CorsPreflightFilter(allowedOriginsCsv, extraOriginsCsv);
    }

    @Bean
    public FilterRegistrationBean<CorsPreflightFilter> corsPreflightFilterRegistration(CorsPreflightFilter filter) {
        FilterRegistrationBean<CorsPreflightFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
