package br.com.comuminha.controller;

import br.com.comuminha.domain.enums.AdType;
import br.com.comuminha.dto.AdResponse;
import br.com.comuminha.dto.CreateAdRequest;
import br.com.comuminha.service.AdService;
import br.com.comuminha.util.SecurityUtil;
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

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Tag(name = "Anúncios", description = "Gerenciamento de anúncios")
@SecurityRequirement(name = "bearer-jwt")
public class AdController {

    private final AdService adService;

    @PostMapping
    @Operation(summary = "Criar anúncio", description = "Cria um novo anúncio na comunidade especificada")
    public ResponseEntity<AdResponse> create(@Valid @RequestBody CreateAdRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.create(userId, request));
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

    @PatchMapping("/{id}/close")
    @Operation(summary = "Encerrar anúncio", description = "Encerra um anúncio (apenas o criador pode encerrar)")
    public ResponseEntity<AdResponse> close(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(adService.closeAd(id, userId));
    }
}
