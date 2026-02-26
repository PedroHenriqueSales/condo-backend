package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    void deleteByReporterUser_Id(Long userId);

    void deleteByAd_Id(Long adId);

    boolean existsByAd_IdAndReporterUser_Id(Long adId, Long reporterUserId);

    @Query("SELECT COUNT(DISTINCT r.reporterUser.id) FROM Report r WHERE r.ad.id = :adId")
    long countDistinctReporterUserIdsByAdId(@Param("adId") Long adId);
}
