package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.UserCommunity;
import br.com.aquidolado.domain.entity.UserCommunity.UserCommunityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, UserCommunityId> {

    List<UserCommunity> findByUserId(Long userId);
}

