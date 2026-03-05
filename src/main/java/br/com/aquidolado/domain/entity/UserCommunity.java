package br.com.aquidolado.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_communities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserCommunity.UserCommunityId.class)
public class UserCommunity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "last_ads_seen_at")
    private Instant lastAdsSeenAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCommunityId implements Serializable {
        private Long userId;
        private Long communityId;
    }
}

