package br.com.aquidolado.controller;

import br.com.aquidolado.dto.NotificationResponse;
import br.com.aquidolado.dto.NotificationSummaryResponse;
import br.com.aquidolado.service.NotificationService;
import br.com.aquidolado.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Central de notificações do usuário")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/summary")
    @Operation(summary = "Resumo de notificações", description = "Retorna contador de não lidas, últimas notificações e novos anúncios por comunidade")
    public ResponseEntity<NotificationSummaryResponse> getSummary() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(notificationService.getSummary(userId));
    }

    @GetMapping
    @Operation(summary = "Listar notificações", description = "Lista notificações do usuário com opção de filtrar somente não lidas")
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(notificationService.listNotifications(userId, unreadOnly, pageable));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida", description = "Marca uma notificação específica como lida")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Marcar todas como lidas", description = "Marca todas as notificações do usuário como lidas")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ads/mark-viewed")
    @Operation(summary = "Marcar anúncios como vistos", description = "Atualiza o último acesso aos anúncios por comunidade")
    public ResponseEntity<Void> markAdsAsViewed(
            @RequestParam(name = "communityId", required = false) Long communityId) {
        Long userId = SecurityUtil.getCurrentUserId();
        notificationService.markAdsAsViewed(userId, Optional.ofNullable(communityId));
        return ResponseEntity.noContent().build();
    }
}

