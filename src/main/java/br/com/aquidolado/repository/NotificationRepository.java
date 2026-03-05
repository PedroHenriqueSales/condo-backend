package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUser_IdAndReadAtIsNull(Long userId);

    List<Notification> findTop20ByUser_IdOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Notification> findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

}

