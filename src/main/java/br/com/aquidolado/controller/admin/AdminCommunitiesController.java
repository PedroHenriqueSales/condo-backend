package br.com.aquidolado.controller.admin;

import br.com.aquidolado.dto.MemberSummary;
import br.com.aquidolado.dto.admin.AdminCommunityListItem;
import br.com.aquidolado.dto.admin.AdminCommunityMapItem;
import br.com.aquidolado.util.SecurityUtil;
import br.com.aquidolado.service.AdminCommunityService;
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
@Tag(name = "Admin - Comunidades", description = "Gestão de comunidades (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminCommunitiesController {

    private final AdminCommunityService adminCommunityService;

    @GetMapping("/communities")
    @Operation(summary = "Listar comunidades", description = "Lista comunidades com paginação")
    public ResponseEntity<Page<AdminCommunityListItem>> listCommunities(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminCommunityService.listCommunities(pageable));
    }

    @GetMapping("/communities/map")
    @Operation(summary = "Comunidades para mapa", description = "Lista comunidades com coordenadas para exibição no mapa")
    public ResponseEntity<List<AdminCommunityMapItem>> getMapItems() {
        return ResponseEntity.ok(adminCommunityService.getMapItems());
    }

    @GetMapping("/communities/{id}")
    @Operation(summary = "Detalhe da comunidade")
    public ResponseEntity<AdminCommunityListItem> getCommunity(@PathVariable Long id) {
        return ResponseEntity.ok(adminCommunityService.getCommunity(id));
    }

    @GetMapping("/communities/{id}/members")
    @Operation(summary = "Membros da comunidade")
    public ResponseEntity<List<MemberSummary>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(adminCommunityService.getMembers(id));
    }

    @DeleteMapping("/communities/{id}")
    @Operation(summary = "Excluir comunidade", description = "Remove a comunidade e todos os dados associados")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id) {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        adminCommunityService.deleteCommunity(id, adminUserId);
        return ResponseEntity.noContent().build();
    }
}
