package br.com.aquidolado.controller;

import br.com.aquidolado.dto.UpdateProfileRequest;
import br.com.aquidolado.dto.UserProfileResponse;
import br.com.aquidolado.service.UserService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuário", description = "Endpoints para perfil do usuário autenticado")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil", description = "Retorna os dados do usuário autenticado")
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil", description = "Atualiza nome e telefone do usuário")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }
}
