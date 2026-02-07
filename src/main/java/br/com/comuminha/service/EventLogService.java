package br.com.comuminha.service;

import br.com.comuminha.domain.entity.Community;
import br.com.comuminha.domain.entity.EventLog;
import br.com.comuminha.domain.entity.User;
import br.com.comuminha.domain.enums.EventType;
import br.com.comuminha.repository.CommunityRepository;
import br.com.comuminha.repository.EventLogRepository;
import br.com.comuminha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    @Transactional
    public void log(EventType eventType, Long userId, Long communityId) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        Community community = communityId != null ? communityRepository.findById(communityId).orElse(null) : null;

        EventLog event = EventLog.builder()
                .eventType(eventType)
                .user(user)
                .community(community)
                .createdAt(java.time.Instant.now())
                .build();

        eventLogRepository.save(event);
    }
}
