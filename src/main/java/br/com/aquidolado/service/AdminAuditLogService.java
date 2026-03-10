package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.AdminAuditLog;
import br.com.aquidolado.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository repository;

    public void log(Long adminUserId, String action, String entityType, String entityId, String payload) {
        try {
            AdminAuditLog log = AdminAuditLog.builder()
                    .userId(adminUserId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .payload(payload != null && payload.length() > 2000 ? payload.substring(0, 2000) : payload)
                    .createdAt(java.time.Instant.now())
                    .build();
            repository.save(log);
        } catch (Exception ignored) {
            // não falhar a operação principal por causa do log
        }
    }
}
