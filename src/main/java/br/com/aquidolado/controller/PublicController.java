package br.com.aquidolado.controller;

import br.com.aquidolado.dto.AdOgResponse;
import br.com.aquidolado.service.AdService;
import br.com.aquidolado.service.SystemSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Público", description = "Endpoints sem autenticação (ex.: preview para compartilhamento)")
public class PublicController {

    private final AdService adService;
    private final SystemSettingsService systemSettingsService;

    @GetMapping("/features")
    @Operation(summary = "Features públicas", description = "Flags de funcionalidade (ex.: adsEnabled para exibir ou não AdSense). Público.")
    public ResponseEntity<Map<String, Object>> getFeatures() {
        return ResponseEntity.ok(systemSettingsService.getPublicFeatures());
    }

    @GetMapping("/ads/{id}/og")
    @Operation(summary = "Dados OG do anúncio", description = "Retorna título e path da primeira imagem para montar preview (WhatsApp, etc.). Público.")
    public ResponseEntity<AdOgResponse> getAdOg(@PathVariable Long id) {
        return adService.getOgData(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
