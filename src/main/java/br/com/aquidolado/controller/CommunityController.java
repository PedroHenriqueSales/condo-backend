package br.com.aquidolado.controller;

import br.com.aquidolado.dto.CommunityResponse;
import br.com.aquidolado.dto.CreateCommunityRequest;
import br.com.aquidolado.dto.JoinCommunityRequest;
import br.com.aquidolado.service.CommunityService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Condomínios", description = "Gerenciamento de condomínios/comunidades")
@SecurityRequirement(name = "bearer-jwt")
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @Operation(summary = "Criar condomínio", description = "Cria um novo condomínio/comunidade e gera um código de acesso")
    public ResponseEntity<CommunityResponse> create(@Valid @RequestBody CreateCommunityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.create(userId, request));
    }

    @PostMapping("/join")
    @Operation(summary = "Entrar em condomínio", description = "Entra em um condomínio usando o código de acesso")
    public ResponseEntity<CommunityResponse> join(@Valid @RequestBody JoinCommunityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.joinByAccessCode(userId, request.getAccessCode()));
    }

    @GetMapping
    @Operation(summary = "Listar meus condomínios", description = "Retorna a lista de condomínios do usuário autenticado")
    public ResponseEntity<List<CommunityResponse>> listMyCommunities() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.listUserCommunities(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes do condomínio", description = "Retorna os detalhes de um condomínio específico")
    public ResponseEntity<CommunityResponse> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.getById(id, userId));
    }

    @DeleteMapping("/{id}/leave")
    @Operation(summary = "Deixar condomínio", description = "Remove o usuário atual do condomínio")
    public ResponseEntity<Void> leave(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.leave(userId, id);
        return ResponseEntity.noContent().build();
    }
}
