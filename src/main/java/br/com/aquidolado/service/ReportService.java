package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.Report;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.dto.ReportRequest;
import br.com.aquidolado.exception.AlreadyReportedException;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.ReportRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final EventLogService eventLogService;

    @Value("${app.reports.suspend-threshold:2}")
    private int suspendThreshold;

    @Value("${app.reports.remove-threshold:4}")
    private int removeThreshold;

    @Transactional
    public void report(Long userId, ReportRequest request) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Ad ad = adRepository.findById(request.getAdId())
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não pode denunciar anúncios de comunidades que não participa");
        }

        if (reportRepository.existsByAd_IdAndReporterUser_Id(ad.getId(), userId)) {
            throw new AlreadyReportedException();
        }

        Report report = Report.builder()
                .ad(ad)
                .reason(request.getReason())
                .reporterUser(reporter)
                .createdAt(Instant.now())
                .build();

        reportRepository.save(report);
        eventLogService.log(EventType.REPORT_AD, userId, ad.getCommunity().getId());

        long distinctReporters = reportRepository.countDistinctReporterUserIdsByAdId(ad.getId());

        if (distinctReporters >= removeThreshold) {
            ad.setStatus(AdStatus.REMOVED);
            ad.setSuspendedByReportsAt(null);
            adRepository.save(ad);
        } else if (distinctReporters >= suspendThreshold) {
            ad.setStatus(AdStatus.PAUSED);
            ad.setSuspendedByReportsAt(Instant.now());
            adRepository.save(ad);
        }
    }
}
