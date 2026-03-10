package br.com.aquidolado.controller.admin;

import br.com.aquidolado.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Estatísticas do sistema (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas", description = "Totais de usuários, comunidades, anúncios e denúncias")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }
}
