package br.com.aquidolado.controller;

import br.com.aquidolado.domain.enums.AdType;
import br.com.aquidolado.dto.AdResponse;
import br.com.aquidolado.dto.CommentResponse;
import br.com.aquidolado.dto.CreateAdRequest;
import br.com.aquidolado.dto.CreateCommentRequest;
import br.com.aquidolado.dto.ReactionRequest;
import br.com.aquidolado.dto.UpdateAdRequest;
import br.com.aquidolado.service.AdService;
import br.com.aquidolado.service.RecommendationCommentService;
import br.com.aquidolado.service.RecommendationReactionService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Anúncios", description = "Gerenciamento de anúncios")
@SecurityRequirement(name = "bearer-jwt")
public class AdController {

    private final AdService adService;
    private final RecommendationReactionService recommendationReactionService;
    private final RecommendationCommentService recommendationCommentService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Criar anúncio", description = "Cria um novo anúncio na comunidade (até 5 imagens)")
    public ResponseEntity<AdResponse> create(
            @Valid @RequestPart("ad") CreateAdRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.create(userId, request, images));
    }

    @GetMapping
    @Operation(summary = "Listar anúncios", description = "Lista anúncios de uma comunidade com filtros opcionais (tipo(s), busca)")
    public ResponseEntity<Page<AdResponse>> listByCommunity(
            @RequestParam Long communityId,
            @RequestParam(required = false) AdType type,
            @RequestParam(required = false) List<AdType> types,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        // Se types foi fornecido, usa ele; senão, se type foi fornecido, cria lista com um elemento; senão null
        List<AdType> typesToUse = (types != null && !types.isEmpty()) ? types 
                : (type != null ? List.of(type) : null);
        return ResponseEntity.ok(adService.listByCommunity(communityId, userId, typesToUse, search, pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "Meus anúncios", description = "Lista todos os anúncios criados pelo usuário autenticado")
    public ResponseEntity<Page<AdResponse>> listMyAds(@PageableDefault(size = 20) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.listMyAds(userId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes do anúncio", description = "Retorna os detalhes de um anúncio específico")
    public ResponseEntity<AdResponse> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.getById(id, userId));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @Operation(summary = "Editar anúncio", description = "Atualiza um anúncio (apenas o criador). Envie images para substituir.")
    public ResponseEntity<AdResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("ad") UpdateAdRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.update(id, userId, request, images));
    }

    @PatchMapping("/{id}/pause")
    @Operation(summary = "Pausar anúncio", description = "Pausa um anúncio ativo (não aparece no feed até reativar)")
    public ResponseEntity<AdResponse> pause(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.pauseAd(id, userId));
    }

    @PatchMapping("/{id}/unpause")
    @Operation(summary = "Reativar anúncio", description = "Reativa um anúncio pausado")
    public ResponseEntity<AdResponse> unpause(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.unpauseAd(id, userId));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Encerrar anúncio", description = "Encerra um anúncio (apenas o criador pode encerrar)")
    public ResponseEntity<AdResponse> close(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.closeAd(id, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover anúncio", description = "Exclui permanentemente um anúncio encerrado (apenas o criador)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        adService.deleteAd(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reaction")
    @Operation(summary = "Definir reação (indicação)", description = "Curtir ou descurtir uma indicação (apenas tipo RECOMMENDATION)")
    public ResponseEntity<Void> setReaction(@PathVariable Long id, @Valid @RequestBody ReactionRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        recommendationReactionService.setReaction(id, userId, request.getKind());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/reaction")
    @Operation(summary = "Remover reação (indicação)", description = "Remove sua reação da indicação")
    public ResponseEntity<Void> removeReaction(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        recommendationReactionService.removeReaction(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Listar comentários (indicação)", description = "Lista comentários da indicação (apenas tipo RECOMMENDATION)")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long id,
            @PageableDefault(size = 50) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(recommendationCommentService.getComments(id, userId, pageable));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Criar comentário (indicação)", description = "Adiciona um comentário à indicação")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(recommendationCommentService.createComment(id, userId, request));
    }

    @PostMapping("/{id}/comments/{commentId}/like")
    @Operation(summary = "Curtir comentário (toggle)", description = "Curtir ou descurtir um comentário da indicação")
    public ResponseEntity<Void> toggleCommentLike(
            @PathVariable Long id,
            @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        recommendationCommentService.toggleCommentLike(id, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @Operation(summary = "Excluir comentário", description = "Exclui um comentário da indicação (somente o autor)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        recommendationCommentService.deleteComment(id, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
