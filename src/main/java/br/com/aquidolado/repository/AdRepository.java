package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.AdType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdRepository extends JpaRepository<Ad, Long> {

    Page<Ad> findByCommunityIdAndStatus(Long communityId, AdStatus status, Pageable pageable);

    @Query("SELECT a FROM Ad a " +
           "JOIN FETCH a.user JOIN FETCH a.community " +
           "WHERE a.user.id = :userId")
    Page<Ad> findByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Ad a " +
           "JOIN FETCH a.user JOIN FETCH a.community " +
           "WHERE a.community.id = :communityId AND a.status = :status " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:searchPattern IS NULL OR LOWER(a.title) LIKE :searchPattern " +
           "OR LOWER(COALESCE(a.description, '')) LIKE :searchPattern)")
    Page<Ad> findByCommunityWithFilters(
            @Param("communityId") Long communityId,
            @Param("status") AdStatus status,
            @Param("type") AdType type,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

}
