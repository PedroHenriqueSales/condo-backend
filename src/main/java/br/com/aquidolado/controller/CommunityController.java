package br.com.aquidolado.controller;

import br.com.aquidolado.dto.CommunityResponse;
import br.com.aquidolado.dto.CreateCommunityRequest;
import br.com.aquidolado.dto.JoinCommunityRequest;
import br.com.aquidolado.dto.JoinRequestResponse;
import br.com.aquidolado.dto.AddAdminRequest;
import br.com.aquidolado.dto.UpdateCommunityRequest;
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

    @GetMapping("/admin")
    @Operation(summary = "Listar comunidades que administro", description = "Retorna as comunidades em que o usuário é administrador")
    public ResponseEntity<List<CommunityResponse>> listAdminCommunities() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.listAdminCommunities(userId));
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

    @GetMapping("/{id}/admin/requests")
    @Operation(summary = "Solicitações pendentes", description = "Lista solicitações de entrada pendentes (apenas administrador)")
    public ResponseEntity<List<JoinRequestResponse>> getPendingRequests(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.getPendingRequests(id, userId));
    }

    @PostMapping("/{id}/admin/requests/{requestId}/approve")
    @Operation(summary = "Aprovar solicitação", description = "Aprova uma solicitação de entrada (apenas administrador)")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id, @PathVariable Long requestId) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.approveRequest(id, requestId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/admin/requests/{requestId}/reject")
    @Operation(summary = "Rejeitar solicitação", description = "Rejeita uma solicitação de entrada (apenas administrador)")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id, @PathVariable Long requestId) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.rejectRequest(id, requestId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/admin/admins")
    @Operation(summary = "Adicionar administrador", description = "Torna um membro administrador da comunidade (apenas administrador)")
    public ResponseEntity<Void> addAdmin(@PathVariable Long id, @Valid @RequestBody AddAdminRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.addAdmin(id, request.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/admin/members/{memberId}")
    @Operation(summary = "Remover membro", description = "Remove um membro da comunidade (apenas administrador). Não pode remover a si mesmo.")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.removeMember(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/admin/me")
    @Operation(summary = "Deixar de ser administrador", description = "Remove o usuário atual dos administradores da comunidade, permanecendo como membro")
    public ResponseEntity<Void> leaveAdminRole(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.leaveAdminRole(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/admin")
    @Operation(summary = "Atualizar nome da comunidade", description = "Altera o nome da comunidade (apenas administrador)")
    public ResponseEntity<CommunityResponse> updateCommunity(@PathVariable Long id, @Valid @RequestBody UpdateCommunityRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.updateName(id, userId, request));
    }

    @PostMapping("/{id}/admin/regenerate-access-code")
    @Operation(summary = "Gerar novo código de acesso", description = "Gera um novo código de acesso; o código atual deixa de funcionar (apenas administrador)")
    public ResponseEntity<CommunityResponse> regenerateAccessCode(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(communityService.regenerateAccessCode(id, userId));
    }

    @DeleteMapping("/{id}/admin")
    @Operation(summary = "Apagar comunidade", description = "Apaga a comunidade. Permitido somente quando o administrador for o único membro.")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        communityService.deleteCommunity(id, userId);
        return ResponseEntity.noContent().build();
    }
}
