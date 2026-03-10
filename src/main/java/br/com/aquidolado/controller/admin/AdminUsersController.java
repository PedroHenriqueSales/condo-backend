package br.com.aquidolado.controller.admin;

import br.com.aquidolado.dto.admin.AdminUserDetailResponse;
import br.com.aquidolado.dto.admin.AdminUserListItem;
import br.com.aquidolado.dto.admin.AdminUserPatchRequest;
import br.com.aquidolado.util.SecurityUtil;
import br.com.aquidolado.service.AdminUserService;
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
@Tag(name = "Admin - Usuários", description = "Gestão de usuários (apenas ROLE_ADMIN)")
@SecurityRequirement(name = "bearer-jwt")
public class AdminUsersController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @Operation(summary = "Listar usuários", description = "Lista usuários com paginação")
    public ResponseEntity<Page<AdminUserListItem>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.listUsers(pageable));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Detalhe do usuário")
    public ResponseEntity<AdminUserDetailResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @PatchMapping("/users/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza active, emailVerified, etc.")
    public ResponseEntity<AdminUserDetailResponse> patchUser(
            @PathVariable Long id,
            @RequestBody AdminUserPatchRequest request) {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adminUserService.patchUser(id, request, adminUserId));
    }
}
