package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.RecommendationReaction;
import br.com.aquidolado.domain.enums.ReactionKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecommendationReactionRepository extends JpaRepository<RecommendationReaction, Long> {

    long countByAdIdAndKind(Long adId, ReactionKind kind);

    Optional<RecommendationReaction> findByAdIdAndUserId(Long adId, Long userId);

    void deleteByAdIdAndUserId(Long adId, Long userId);

}
