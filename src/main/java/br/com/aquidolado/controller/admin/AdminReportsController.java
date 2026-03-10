package br.com.aquidolado.controller.admin;

import br.com.aquidolado.dto.admin.AdminReportListItem;
import br.com.aquidolado.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Denúncias", description = "Moderação de denúncias (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminReportsController {

    private final AdminReportService adminReportService;

    @GetMapping("/reports")
    @Operation(summary = "Listar denúncias", description = "Lista denúncias com paginação")
    public ResponseEntity<Page<AdminReportListItem>> listReports(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminReportService.listReports(pageable));
    }

    @GetMapping("/ads/{adId}/reports")
    @Operation(summary = "Denúncias de um anúncio")
    public ResponseEntity<List<AdminReportListItem>> listReportsByAd(@PathVariable Long adId) {
        return ResponseEntity.ok(adminReportService.listReportsByAd(adId));
    }
}
