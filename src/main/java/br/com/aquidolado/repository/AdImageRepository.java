package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.AdImage;

import java.util.List;

public interface AdImageRepository extends org.springframework.data.jpa.repository.JpaRepository<AdImage, Long> {

    List<AdImage> findByAdIdOrderBySortOrder(Long adId);

    void deleteByAdId(Long adId);
}
