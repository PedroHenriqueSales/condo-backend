package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.dto.UpdateProfileRequest;
import br.com.aquidolado.dto.UserProfileResponse;
import br.com.aquidolado.repository.*;
import br.com.aquidolado.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ReportRepository reportRepository;
    private final RecommendationReactionRepository recommendationReactionRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RecommendationCommentRepository recommendationCommentRepository;
    private final AdImageRepository adImageRepository;
    private final AdRepository adRepository;
    private final CommunityAdminRepository communityAdminRepository;
    private final CommunityJoinRequestRepository communityJoinRequestRepository;
    private final EventLogRepository eventLogRepository;
    private final CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .whatsapp(user.getWhatsapp())
                .address(user.getAddress())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setName(request.getName().trim());
        user.setWhatsapp(PhoneUtil.normalize(request.getWhatsapp()));
        user.setAddress(request.getAddress() != null && !request.getAddress().isBlank()
                ? request.getAddress().trim()
                : null);

        user = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .whatsapp(user.getWhatsapp())
                .address(user.getAddress())
                .build();
    }

    /**
     * Exclui a conta do usuário e todos os dados associados (LGPD, Art. 18, VI).
     * Ordem: tokens, denúncias, reações, curtidas, comentários, anúncios (e dependências),
     * admins, pedidos de entrada, logs, comunidades do usuário, comunidades criadas por ele, usuário.
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        tokenService.deleteAllTokensForUser(userId);
        reportRepository.deleteByReporterUser_Id(userId);
        recommendationReactionRepository.deleteByUser_Id(userId);
        commentLikeRepository.deleteByUser_Id(userId);
        recommendationCommentRepository.deleteByUser_Id(userId);

        List<Ad> userAds = adRepository.findByUser_Id(userId);
        for (Ad ad : userAds) {
            reportRepository.deleteByAd_Id(ad.getId());
            recommendationReactionRepository.deleteByAd_Id(ad.getId());
            recommendationCommentRepository.findByAd_Id(ad.getId()).forEach(comment -> commentLikeRepository.deleteByCommentId(comment.getId()));
            recommendationCommentRepository.deleteByAd_Id(ad.getId());
            adImageRepository.deleteByAdId(ad.getId());
            adRepository.delete(ad);
        }

        communityAdminRepository.deleteByUser_Id(userId);
        communityJoinRequestRepository.deleteByUser_Id(userId);
        eventLogRepository.deleteByUser_Id(userId);

        user.getCommunities().clear();
        userRepository.saveAndFlush(user);

        List<Community> createdByUser = communityRepository.findByCreatedBy_Id(userId);
        for (Community community : createdByUser) {
            deleteCommunityAndContents(community.getId());
        }

        userRepository.delete(user);
        log.info("Conta excluída - UserId: {}", userId);
    }

    private void deleteCommunityAndContents(Long communityId) {
        List<Ad> ads = adRepository.findByCommunity_Id(communityId);
        for (Ad ad : ads) {
            reportRepository.deleteByAd_Id(ad.getId());
            recommendationReactionRepository.deleteByAd_Id(ad.getId());
            recommendationCommentRepository.findByAd_Id(ad.getId()).forEach(comment -> commentLikeRepository.deleteByCommentId(comment.getId()));
            recommendationCommentRepository.deleteByAd_Id(ad.getId());
            adImageRepository.deleteByAdId(ad.getId());
            adRepository.delete(ad);
        }
        communityAdminRepository.deleteByCommunity_Id(communityId);
        communityJoinRequestRepository.deleteByCommunity_Id(communityId);
        communityRepository.deleteById(communityId);
    }
}
