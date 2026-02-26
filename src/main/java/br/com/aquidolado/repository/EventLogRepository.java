package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    void deleteByUser_Id(Long userId);
}
