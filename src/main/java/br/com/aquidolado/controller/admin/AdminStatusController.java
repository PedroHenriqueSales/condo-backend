package br.com.aquidolado.controller.admin;

import br.com.aquidolado.service.AdminStatusService;
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
@Tag(name = "Admin - Status", description = "Status dos serviços (backend, Vercel, Railway)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminStatusController {

    private final AdminStatusService adminStatusService;

    @GetMapping("/status")
    @Operation(summary = "Status dos serviços", description = "Retorna status do backend e, se configurado, Vercel e Railway")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(adminStatusService.getStatus());
    }
}
