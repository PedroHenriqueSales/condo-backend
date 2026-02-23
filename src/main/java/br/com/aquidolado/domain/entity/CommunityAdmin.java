package br.com.aquidolado.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "community_admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CommunityAdmin.CommunityAdminId.class)
public class CommunityAdmin {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityAdminId implements Serializable {
        private Long community;
        private Long user;
    }
}