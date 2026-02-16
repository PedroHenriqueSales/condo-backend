package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.RecommendationReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecommendationReactionRepository extends JpaRepository<RecommendationReaction, Long> {

    long countByAdId(Long adId);

    @Query("SELECT AVG(r.rating) FROM RecommendationReaction r WHERE r.ad.id = :adId")
    Double getAverageRatingByAdId(@Param("adId") Long adId);

    Optional<RecommendationReaction> findByAdIdAndUserId(Long adId, Long userId);

    void deleteByAdIdAndUserId(Long adId, Long userId);

}
