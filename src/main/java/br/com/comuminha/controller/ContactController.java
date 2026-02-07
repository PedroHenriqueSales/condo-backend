package br.com.comuminha.controller;

import br.com.comuminha.domain.enums.EventType;
import br.com.comuminha.dto.ContactClickRequest;
import br.com.comuminha.service.EventLogService;
import br.com.comuminha.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contato", description = "Registro de cliques em contato")
@SecurityRequirement(name = "bearer-jwt")
public class ContactController {

    private final EventLogService eventLogService;

    @PostMapping("/click")
    @Operation(summary = "Registrar clique em contato", description = "Registra um evento quando o usuário clica em 'Entrar em contato'")
    public ResponseEntity<Void> registerContactClick(@Valid @RequestBody ContactClickRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        eventLogService.log(EventType.CONTACT_CLICK, userId, request.getCommunityId());
        return ResponseEntity.ok().build();
    }
}
