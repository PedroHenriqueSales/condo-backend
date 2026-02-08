package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.EventLog;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.repository.CommunityRepository;
import br.com.aquidolado.repository.EventLogRepository;
import br.com.aquidolado.repository.UserRepository;
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
