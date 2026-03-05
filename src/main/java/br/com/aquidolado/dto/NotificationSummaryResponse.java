package br.com.aquidolado.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class NotificationSummaryResponse {
    long totalUnread;
    List<NotificationResponse> recentNotifications;
    List<NewAdsByCommunitySummary> newAdsByCommunity;
}

