package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByAccessCode(String accessCode);

    boolean existsByAccessCode(String accessCode);
}
