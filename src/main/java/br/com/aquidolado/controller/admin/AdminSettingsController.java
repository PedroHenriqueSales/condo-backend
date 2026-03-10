package br.com.aquidolado.controller.admin;

import br.com.aquidolado.service.AdminAuditLogService;
import br.com.aquidolado.service.SystemSettingsService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Configurações", description = "Configurações do sistema (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminSettingsController {

    private final SystemSettingsService systemSettingsService;
    private final AdminAuditLogService auditLogService;

    @GetMapping("/settings")
    @Operation(summary = "Obter configurações", description = "Retorna configurações do sistema (ex.: adsEnabled)")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getAdminSettings());
    }

    @PatchMapping("/settings")
    @Operation(summary = "Atualizar configurações", description = "Atualiza configurações (ex.: adsEnabled para ligar/desligar AdSense)")
    public ResponseEntity<Map<String, Object>> patchSettings(@RequestBody Map<String, Object> patch) {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        Map<String, Object> result = systemSettingsService.patchAdminSettings(patch);
        if (patch.containsKey("adsEnabled")) {
            auditLogService.log(adminUserId, "PATCH_SETTINGS", "SystemSettings", "adsEnabled", "adsEnabled=" + patch.get("adsEnabled"));
        }
        return ResponseEntity.ok(result);
    }
}
