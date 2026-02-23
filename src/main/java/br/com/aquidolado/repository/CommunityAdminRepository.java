package br.com.aquidolado.repository;

import br.com.aquidolado.domain.entity.CommunityAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityAdminRepository extends JpaRepository<CommunityAdmin, CommunityAdmin.CommunityAdminId> {

    boolean existsByCommunity_IdAndUser_Id(Long communityId, Long userId);

    List<CommunityAdmin> findByUser_Id(Long userId);

    @Query("SELECT ca FROM CommunityAdmin ca JOIN FETCH ca.community c LEFT JOIN FETCH c.createdBy WHERE ca.user.id = :userId")
    List<CommunityAdmin> findByUser_IdWithCommunityAndCreatedBy(@Param("userId") Long userId);

    @Query("SELECT ca FROM CommunityAdmin ca WHERE ca.community.id = :communityId")
    List<CommunityAdmin> findByCommunity_Id(@Param("communityId") Long communityId);

    void deleteByCommunity_IdAndUser_Id(Long communityId, Long userId);

    long countByCommunity_Id(Long communityId);
}
