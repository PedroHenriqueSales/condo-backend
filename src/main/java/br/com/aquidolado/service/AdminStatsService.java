package br.com.aquidolado.service;

import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.CommunityRepository;
import br.com.aquidolado.repository.ReportRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final AdRepository adRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("usersCount", userRepository.count());
        stats.put("communitiesCount", communityRepository.count());
        stats.put("adsCount", adRepository.count());
        stats.put("reportsCount", reportRepository.count());
        return stats;
    }
}
