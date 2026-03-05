package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class NotificationResponse {
    Long id;
    NotificationType type;
    String title;
    String body;
    Long adId;
    Long communityId;
    String communityName;
    Long joinRequestId;
    Long reportId;
    Instant createdAt;
    Instant readAt;
}

