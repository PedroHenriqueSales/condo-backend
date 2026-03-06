package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.AccessCodeRequest;
import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.CommunityAdmin;
import br.com.aquidolado.domain.entity.CommunityJoinRequest;
import br.com.aquidolado.domain.entity.Notification;
import br.com.aquidolado.domain.entity.RecommendationComment;
import br.com.aquidolado.domain.entity.Report;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.entity.UserCommunity;
import br.com.aquidolado.domain.enums.NotificationType;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.dto.NewAdsByCommunitySummary;
import br.com.aquidolado.dto.NotificationResponse;
import br.com.aquidolado.dto.NotificationSummaryResponse;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.CommunityAdminRepository;
import br.com.aquidolado.repository.CommunityRepository;
import br.com.aquidolado.repository.NotificationRepository;
import br.com.aquidolado.repository.RecommendationCommentRepository;
import br.com.aquidolado.repository.UserCommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RecommendationCommentRepository recommendationCommentRepository;
    private final CommunityAdminRepository communityAdminRepository;
    private final UserCommunityRepository userCommunityRepository;
    private final AdRepository adRepository;
    private final CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummary(Long userId) {
        long totalUnread = notificationRepository.countByUser_IdAndReadAtIsNull(userId);
        List<Notification> recent = notificationRepository.findTop20ByUser_IdOrderByCreatedAtDesc(userId);

        List<NotificationResponse> recentDtos = recent.stream()
                .map(this::toNotificationResponse)
                .toList();

        List<NewAdsByCommunitySummary> newAdsByCommunity = computeNewAdsByCommunity(userId);

        return NotificationSummaryResponse.builder()
                .totalUnread(totalUnread)
                .recentNotifications(recentDtos)
                .newAdsByCommunity(newAdsByCommunity)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listNotifications(Long userId, boolean unreadOnly, Pageable pageable) {
        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        return page.map(this::toNotificationResponse);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode alterar esta notificação");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findTop20ByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> n.getReadAt() == null)
                .toList();
        Instant now = Instant.now();
        for (Notification n : unread) {
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAdsAsViewed(Long userId, Optional<Long> communityIdOpt) {
        Instant now = Instant.now();

        if (communityIdOpt.isPresent()) {
            Long communityId = communityIdOpt.get();
            UserCommunity.UserCommunityId id = new UserCommunity.UserCommunityId(userId, communityId);
            UserCommunity uc = userCommunityRepository.findById(id)
                    .orElseGet(() -> UserCommunity.builder()
                            .userId(userId)
                            .communityId(communityId)
                            .build());
            uc.setLastAdsSeenAt(now);
            userCommunityRepository.save(uc);
        } else {
            List<UserCommunity> list = userCommunityRepository.findByUserId(userId);
            for (UserCommunity uc : list) {
                uc.setLastAdsSeenAt(now);
            }
            userCommunityRepository.saveAll(list);
        }
    }

    private List<NewAdsByCommunitySummary> computeNewAdsByCommunity(Long userId) {
        List<UserCommunity> memberships = userCommunityRepository.findByUserId(userId);
        Map<Long, Instant> lastSeenByCommunity = new HashMap<>();
        for (UserCommunity uc : memberships) {
            lastSeenByCommunity.put(uc.getCommunityId(), uc.getLastAdsSeenAt());
        }

        List<Long> communityIds = memberships.stream()
                .map(UserCommunity::getCommunityId)
                .distinct()
                .toList();
        Map<Long, String> communityNames = new HashMap<>();
        if (!communityIds.isEmpty()) {
            communityRepository.findAllById(communityIds)
                    .forEach(c -> communityNames.put(c.getId(), c.getName()));
        }

        return communityIds.stream()
                .map(communityId -> {
                    Instant lastSeen = lastSeenByCommunity.get(communityId);
                    if (lastSeen == null) {
                        // Nunca viu: conta todos os anúncios ativos
                        long count = adRepository.countByCommunity_IdAndStatusAndCreatedAtAfter(
                                communityId, AdStatus.ACTIVE, Instant.EPOCH);
                        return NewAdsByCommunitySummary.builder()
                                .communityId(communityId)
                                .communityName(communityNames.get(communityId))
                                .newAdsCount(count)
                                .build();
                    } else {
                        long count = adRepository.countByCommunity_IdAndStatusAndCreatedAtAfter(
                                communityId, AdStatus.ACTIVE, lastSeen);
                        return NewAdsByCommunitySummary.builder()
                                .communityId(communityId)
                                .communityName(communityNames.get(communityId))
                                .newAdsCount(count)
                                .build();
                    }
                })
                .filter(s -> s.getNewAdsCount() > 0)
                .toList();
    }

    private NotificationResponse toNotificationResponse(Notification n) {
        Community community = n.getCommunity();
        String accessCode = (n.getType() == NotificationType.ACCESS_CODE_GRANTED && community != null)
                ? community.getAccessCode() : null;
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .adId(n.getAd() != null ? n.getAd().getId() : null)
                .communityId(community != null ? community.getId() : null)
                .communityName(community != null ? community.getName() : null)
                .joinRequestId(n.getJoinRequest() != null ? n.getJoinRequest().getId() : null)
                .reportId(n.getReport() != null ? n.getReport().getId() : null)
                .accessCode(accessCode)
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }

    @Transactional
    public void notifyCommentCreated(RecommendationComment comment) {
        Ad ad = comment.getAd();
        User commentAuthor = comment.getUser();
        User adOwner = ad.getUser();

        Instant now = Instant.now();

        // Notifica o autor do anúncio (se não for o próprio autor do comentário)
        if (!adOwner.getId().equals(commentAuthor.getId())) {
            Notification notificationForOwner = Notification.builder()
                    .user(adOwner)
                    .type(NotificationType.COMMENT_ON_MY_AD)
                    .title("Novo comentário no seu anúncio")
                    .body(commentAuthor.getName() + " comentou em \"" + ad.getTitle() + "\"")
                    .ad(ad)
                    .comment(comment)
                    .community(ad.getCommunity())
                    .createdAt(now)
                    .build();
            notificationRepository.save(notificationForOwner);
        }

        // Notifica outros participantes do anúncio (quem já comentou antes)
        List<RecommendationComment> previousComments = recommendationCommentRepository.findByAd_Id(ad.getId());
        Set<Long> notifiedUserIds = new HashSet<>();
        notifiedUserIds.add(adOwner.getId());
        notifiedUserIds.add(commentAuthor.getId());

        for (RecommendationComment previous : previousComments) {
            User participant = previous.getUser();
            if (participant == null || notifiedUserIds.contains(participant.getId())) {
                continue;
            }
            notifiedUserIds.add(participant.getId());

            Notification notificationForParticipant = Notification.builder()
                    .user(participant)
                    .type(NotificationType.COMMENT_ON_PARTICIPATED_AD)
                    .title("Novo comentário em anúncio que você acompanha")
                    .body(commentAuthor.getName() + " comentou em \"" + ad.getTitle() + "\"")
                    .ad(ad)
                    .comment(comment)
                    .community(ad.getCommunity())
                    .createdAt(now)
                    .build();
            notificationRepository.save(notificationForParticipant);
        }
    }

    @Transactional
    public void notifyCommunityJoinRequest(CommunityJoinRequest request) {
        Community community = request.getCommunity();
        User requester = request.getUser();

        Instant now = Instant.now();

        List<CommunityAdmin> admins = communityAdminRepository.findByCommunity_Id(community.getId());
        for (CommunityAdmin admin : admins) {
            User adminUser = admin.getUser();
            if (adminUser == null) {
                continue;
            }
            Notification notification = Notification.builder()
                    .user(adminUser)
                    .type(NotificationType.COMMUNITY_JOIN_REQUEST)
                    .title("Novo pedido de entrada na comunidade")
                    .body(requester.getName() + " pediu acesso a \"" + community.getName() + "\"")
                    .community(community)
                    .joinRequest(request)
                    .createdAt(now)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyReportCreated(Report report) {
        Ad ad = report.getAd();
        User adOwner = ad.getUser();
        Community community = ad.getCommunity();

        // Não notifica se o anúncio não tiver um dono por algum motivo
        if (adOwner == null) {
            return;
        }

        Notification notification = Notification.builder()
                .user(adOwner)
                .type(NotificationType.REPORT_ON_MY_AD)
                .title("Seu anúncio recebeu uma denúncia")
                .body("Seu anúncio \"" + ad.getTitle() + "\" recebeu uma nova denúncia.")
                .ad(ad)
                .report(report)
                .community(community)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyAccessCodeRequest(AccessCodeRequest request) {
        Community community = request.getCommunity();
        User requester = request.getUser();

        Instant now = Instant.now();

        List<CommunityAdmin> admins = communityAdminRepository.findByCommunity_Id(community.getId());
        for (CommunityAdmin admin : admins) {
            User adminUser = admin.getUser();
            if (adminUser == null) {
                continue;
            }
            Notification notification = Notification.builder()
                    .user(adminUser)
                    .type(NotificationType.ACCESS_CODE_REQUEST)
                    .title("Solicitação de código de acesso")
                    .body(requester.getName() + " solicitou o código de acesso da comunidade \"" + community.getName() + "\"")
                    .community(community)
                    .createdAt(now)
                    .build();
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void notifyAccessCodeGranted(User user, Community community) {
        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.ACCESS_CODE_GRANTED)
                .title("Pedido aceito")
                .body("Seu pedido foi aceito. Toque para usar o código e entrar na comunidade \"" + community.getName() + "\".")
                .community(community)
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }
}

