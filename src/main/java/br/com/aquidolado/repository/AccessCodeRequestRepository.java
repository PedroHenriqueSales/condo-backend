package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.AccessCodeRequest;
import br.com.aquidolado.domain.enums.AccessCodeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessCodeRequestRepository extends JpaRepository<AccessCodeRequest, Long> {

    Optional<AccessCodeRequest> findByCommunityIdAndUserId(Long communityId, Long userId);

    List<AccessCodeRequest> findByCommunityIdAndStatus(Long communityId, AccessCodeRequestStatus status);

    boolean existsByCommunityIdAndUserIdAndStatus(Long communityId, Long userId, AccessCodeRequestStatus status);
}
