package br.com.comuminha.repository;

import br.com.comuminha.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCommunitiesId(String email, Long communityId);

    boolean existsByIdAndCommunitiesId(Long id, Long communityId);
}
