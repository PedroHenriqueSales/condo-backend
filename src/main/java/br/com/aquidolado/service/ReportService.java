package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.Report;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.dto.ReportRequest;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.ReportRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void report(Long userId, ReportRequest request) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Ad ad = adRepository.findById(request.getAdId())
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não pode denunciar anúncios de comunidades que não participa");
        }

        Report report = Report.builder()
                .ad(ad)
                .reason(request.getReason())
                .reporterUser(reporter)
                .createdAt(Instant.now())
                .build();

        reportRepository.save(report);
        eventLogService.log(EventType.REPORT_AD, userId, ad.getCommunity().getId());
    }
}
