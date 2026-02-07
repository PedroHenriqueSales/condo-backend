package br.com.comuminha.controller;

import br.com.comuminha.dto.EventLogRequest;
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
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Registro de eventos genéricos")
@SecurityRequirement(name = "bearer-jwt")
public class EventLogController {

    private final EventLogService eventLogService;

    @PostMapping
    @Operation(summary = "Registrar evento", description = "Registra um evento genérico no sistema")
    public ResponseEntity<Void> log(@Valid @RequestBody EventLogRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        eventLogService.log(request.getEventType(), userId, request.getCommunityId());
        return ResponseEntity.ok().build();
    }
}
