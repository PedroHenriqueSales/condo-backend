package br.com.aquidolado.controller.admin;

import br.com.aquidolado.dto.admin.AdminAdListItem;
import br.com.aquidolado.service.AdminAdsService;
import br.com.aquidolado.util.SecurityUtil;
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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Anúncios", description = "Listagem e remoção de anúncios (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminAdsController {

    private final AdminAdsService adminAdsService;
    private final AdminReportService adminReportService;

    @GetMapping("/ads")
    @Operation(summary = "Listar anúncios", description = "Lista todos os anúncios com paginação, para moderação")
    public ResponseEntity<Page<AdminAdListItem>> listAds(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminAdsService.listAds(pageable));
    }

    @PatchMapping("/ads/{adId}/remove")
    @Operation(summary = "Remover anúncio", description = "Define status REMOVED no anúncio (moderação)")
    public ResponseEntity<Void> removeAd(@PathVariable Long adId) {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        adminReportService.forceRemoveAd(adId, adminUserId);
        return ResponseEntity.noContent().build();
    }
}
