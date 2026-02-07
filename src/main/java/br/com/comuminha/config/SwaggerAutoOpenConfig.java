package br.com.comuminha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Abre automaticamente o Swagger UI no navegador padrão
 * quando a aplicação sobe com o profile "dev".
 */
@Component
public class SwaggerAutoOpenConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment environment;
    private final int serverPort;

    public SwaggerAutoOpenConfig(Environment environment,
                                 @Value("${server.port:8080}") int serverPort) {
        this.environment = environment;
        this.serverPort = serverPort;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Só abre automaticamente em ambiente de desenvolvimento
        if (!Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            return;
        }

        String url = "http://localhost:" + serverPort + "/swagger-ui.html";
        
        // Executa em thread separada para não bloquear a inicialização
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // Aguarda 1 segundo para garantir que o servidor está pronto
                openBrowser(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void openBrowser(String url) {
        // Tenta primeiro com Desktop API
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return;
            } catch (Exception ignored) {
                // Se falhar, tenta métodos alternativos
            }
        }

        // Fallback: usa comandos do sistema operacional
        String os = System.getProperty("os.name").toLowerCase();
        Runtime runtime = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                // Windows
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                runtime.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux/Unix
                String[] browsers = {"xdg-open", "gnome-open", "kde-open", "firefox", "opera", "chromium", "google-chrome"};
                for (String browser : browsers) {
                    try {
                        runtime.exec(new String[]{browser, url});
                        break;
                    } catch (Exception ignored) {
                        // Tenta próximo navegador
                    }
                }
            }
        } catch (Exception ignored) {
            // Se todos os métodos falharem, apenas ignora
        }
    }
}

