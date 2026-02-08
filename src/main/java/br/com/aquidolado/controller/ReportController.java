package br.com.aquidolado.controller;

import br.com.aquidolado.dto.ReportRequest;
import br.com.aquidolado.service.ReportService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Denúncias", description = "Sistema de denúncias de anúncios")
@SecurityRequirement(name = "bearer-jwt")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Denunciar anúncio", description = "Registra uma denúncia sobre um anúncio")
    public ResponseEntity<Void> report(@Valid @RequestBody ReportRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        reportService.report(userId, request);
        return ResponseEntity.ok().build();
    }
}
