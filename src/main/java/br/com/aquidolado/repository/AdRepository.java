package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.AdType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    @Query("SELECT a FROM Ad a " +
           "JOIN FETCH a.user JOIN FETCH a.community " +
           "WHERE a.user.id = :userId")
    Page<Ad> findByUserIdWithUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Ad a " +
           "JOIN FETCH a.user JOIN FETCH a.community " +
           "WHERE a.community.id = :communityId AND a.status = :status " +
           "AND (:types IS NULL OR a.type IN :types) " +
           "AND (:searchPattern IS NULL OR LOWER(a.title) LIKE :searchPattern " +
           "OR LOWER(COALESCE(a.description, '')) LIKE :searchPattern " +
           "OR LOWER(a.user.name) LIKE :searchPattern)")
    Page<Ad> findByCommunityWithFilters(
            @Param("communityId") Long communityId,
            @Param("status") AdStatus status,
            @Param("types") List<AdType> types,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

}
