package br.com.aquidolado.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NewAdsByCommunitySummary {
    Long communityId;
    String communityName;
    long newAdsCount;
}

