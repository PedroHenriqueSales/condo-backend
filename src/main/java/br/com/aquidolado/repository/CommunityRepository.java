package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByAccessCode(String accessCode);

    boolean existsByAccessCode(String accessCode);

    @Query("SELECT DISTINCT c FROM Community c LEFT JOIN FETCH c.createdBy LEFT JOIN FETCH c.members WHERE c.id = :id")
    Optional<Community> findByIdWithCreatedByAndMembers(@Param("id") Long id);

    java.util.List<Community> findByCreatedBy_Id(Long createdById);
}
