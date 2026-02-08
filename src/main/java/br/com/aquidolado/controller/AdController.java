package br.com.aquidolado.controller;

import br.com.aquidolado.domain.enums.AdType;
import br.com.aquidolado.dto.AdResponse;
import br.com.aquidolado.dto.CreateAdRequest;
import br.com.aquidolado.dto.UpdateAdRequest;
import br.com.aquidolado.service.AdService;
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

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Criar anúncio", description = "Cria um novo anúncio na comunidade (até 3 imagens)")
    public ResponseEntity<AdResponse> create(
            @Valid @RequestPart("ad") CreateAdRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.create(userId, request, images));
    }

    @GetMapping
    @Operation(summary = "Listar anúncios", description = "Lista anúncios de uma comunidade com filtros opcionais (tipo, busca)")
    public ResponseEntity<Page<AdResponse>> listByCommunity(
            @RequestParam Long communityId,
            @RequestParam(required = false) AdType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.listByCommunity(communityId, userId, type, search, pageable));
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
}
