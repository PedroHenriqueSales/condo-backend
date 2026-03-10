package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {
}
