package br.com.aquidolado.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

/**
 * Cliente SES v2 (API HTTPS) quando app.email.aws-ses-region está definida.
 * Usa URLConnection (sem Apache HttpClient) para evitar conflitos de dependência em produção.
 */
@Configuration
public class AwsSesConfig {

    @Bean(name = "sesV2Client")
    public SesV2Client sesV2Client(@Value("${app.email.aws-ses-region:}") String region) {
        if (!StringUtils.hasText(region)) {
            return null;
        }
        return SesV2Client.builder()
                .region(Region.of(region.trim()))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }
}
