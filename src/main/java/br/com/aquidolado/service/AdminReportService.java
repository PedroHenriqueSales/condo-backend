package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Report;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.dto.admin.AdminReportListItem;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final AdRepository adRepository;
    private final AdminAuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<AdminReportListItem> listReports(Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable p = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return reportRepository.findAll(p).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public java.util.List<AdminReportListItem> listReportsByAd(Long adId) {
        return reportRepository.findByAd_IdOrderByCreatedAtDesc(adId).stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional
    public void forceRemoveAd(Long adId, Long adminUserId) {
        var ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        ad.setStatus(AdStatus.REMOVED);
        adRepository.save(ad);
        auditLogService.log(adminUserId, "FORCE_REMOVE_AD", "Ad", String.valueOf(adId), "title=" + ad.getTitle());
    }

    private AdminReportListItem toListItem(Report r) {
        return AdminReportListItem.builder()
                .id(r.getId())
                .adId(r.getAd().getId())
                .adTitle(r.getAd().getTitle())
                .reason(r.getReason())
                .reporterUserId(r.getReporterUser().getId())
                .reporterUserName(r.getReporterUser().getName())
                .communityId(r.getAd().getCommunity().getId())
                .communityName(r.getAd().getCommunity().getName())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
