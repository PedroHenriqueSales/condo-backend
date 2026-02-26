package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.RecommendationComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationCommentRepository extends JpaRepository<RecommendationComment, Long> {

    Page<RecommendationComment> findByAdIdOrderByCreatedAtAsc(Long adId, Pageable pageable);

    List<RecommendationComment> findByAd_Id(Long adId);

    void deleteByUser_Id(Long userId);

    void deleteByAd_Id(Long adId);
}
