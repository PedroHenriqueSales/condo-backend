package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.CommunityJoinRequest;
import br.com.aquidolado.domain.enums.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityJoinRequestRepository extends JpaRepository<CommunityJoinRequest, Long> {

    List<CommunityJoinRequest> findByCommunityIdAndStatus(Long communityId, JoinRequestStatus status);

    Optional<CommunityJoinRequest> findByCommunityIdAndUserId(Long communityId, Long userId);

    boolean existsByCommunityIdAndUserIdAndStatus(Long communityId, Long userId, JoinRequestStatus status);
}
