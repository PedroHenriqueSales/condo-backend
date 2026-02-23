package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.CommunityAdmin;
import br.com.aquidolado.domain.entity.CommunityJoinRequest;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.JoinRequestStatus;
import br.com.aquidolado.dto.CommunityResponse;
import br.com.aquidolado.dto.CreateCommunityRequest;
import br.com.aquidolado.dto.JoinRequestResponse;
import br.com.aquidolado.dto.MemberSummary;
import br.com.aquidolado.dto.UpdateCommunityRequest;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.CommunityAdminRepository;
import br.com.aquidolado.repository.CommunityJoinRequestRepository;
import br.com.aquidolado.repository.CommunityRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final int ACCESS_CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityAdminRepository communityAdminRepository;
    private final CommunityJoinRequestRepository joinRequestRepository;
    private final AdRepository adRepository;

    @Transactional
    public CommunityResponse create(Long userId, CreateCommunityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String accessCode = generateAccessCode();
        String postalCode = normalizePostalCode(request.getPostalCode());

        Community community = Community.builder()
                .name(request.getName())
                .accessCode(accessCode)
                .isPrivate(request.isPrivate())
                .postalCode(postalCode)
                .createdAt(Instant.now())
                .createdBy(user)
                .build();

        community = communityRepository.save(community);
        user.getCommunities().add(community);
        userRepository.save(user);

        CommunityAdmin admin = CommunityAdmin.builder()
                .community(community)
                .user(user)
                .build();
        communityAdminRepository.save(admin);

        return toResponse(community, userId);
    }

    @Transactional
    public CommunityResponse joinByAccessCode(Long userId, String accessCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Community community = communityRepository.findByAccessCode(accessCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Código de acesso inválido"));

        if (userRepository.existsByIdAndCommunitiesId(userId, community.getId())) {
            throw new IllegalArgumentException("Você já é membro deste condomínio");
        }

        if (community.getIsPrivate()) {
            if (joinRequestRepository.existsByCommunityIdAndUserIdAndStatus(community.getId(), userId, JoinRequestStatus.PENDING)) {
                throw new IllegalArgumentException("Você já possui uma solicitação pendente para esta comunidade");
            }
            CommunityJoinRequest request = CommunityJoinRequest.builder()
                    .community(community)
                    .user(user)
                    .status(JoinRequestStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
            joinRequestRepository.save(request);
            return toResponse(community, userId).toBuilder().joinPending(true).build();
        }

        user.getCommunities().add(community);
        userRepository.save(user);
        return toResponse(community, userId);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> listUserCommunities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return user.getCommunities().stream()
                .map(c -> toResponse(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommunityResponse getById(Long communityId, Long userId) {
        Community community = communityRepository.findByIdWithCreatedByAndMembers(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, communityId)) {
            throw new IllegalArgumentException("Você não tem acesso a este condomínio");
        }

        return toResponseWithDetails(community, userId);
    }

    @Transactional
    public void leave(Long userId, Long communityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));

        if (!user.getCommunities().remove(community)) {
            throw new IllegalArgumentException("Você não é membro deste condomínio");
        }

        if (communityAdminRepository.existsByCommunity_IdAndUser_Id(communityId, userId)) {
            communityAdminRepository.deleteByCommunity_IdAndUser_Id(communityId, userId);
            ensureCommunityHasAdmin(communityId, userId, true);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> listAdminCommunities(Long userId) {
        return communityAdminRepository.findByUser_IdWithCommunityAndCreatedBy(userId).stream()
                .map(ca -> {
                    Community c = ca.getCommunity();
                    return toResponse(c, userId);
                })
                .collect(Collectors.toList());
    }

    private void requireAdmin(Long communityId, Long userId) {
        if (!communityAdminRepository.existsByCommunity_IdAndUser_Id(communityId, userId)) {
            throw new IllegalArgumentException("Acesso negado: você não é administrador desta comunidade");
        }
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getPendingRequests(Long communityId, Long userId) {
        requireAdmin(communityId, userId);
        return joinRequestRepository.findByCommunityIdAndStatus(communityId, JoinRequestStatus.PENDING).stream()
                .map(r -> JoinRequestResponse.builder()
                        .id(r.getId())
                        .userId(r.getUser().getId())
                        .userName(r.getUser().getName())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveRequest(Long communityId, Long requestId, Long userId) {
        requireAdmin(communityId, userId);
        CommunityJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (!request.getCommunity().getId().equals(communityId)) {
            throw new IllegalArgumentException("Solicitação não pertence a esta comunidade");
        }
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Solicitação já foi processada");
        }
        User targetUser = request.getUser();
        Community community = request.getCommunity();
        targetUser.getCommunities().add(community);
        userRepository.save(targetUser);
        request.setStatus(JoinRequestStatus.APPROVED);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(Long communityId, Long requestId, Long userId) {
        requireAdmin(communityId, userId);
        CommunityJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada"));
        if (!request.getCommunity().getId().equals(communityId)) {
            throw new IllegalArgumentException("Solicitação não pertence a esta comunidade");
        }
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Solicitação já foi processada");
        }
        request.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void addAdmin(Long communityId, Long targetUserId, Long userId) {
        requireAdmin(communityId, userId);
        if (!userRepository.existsByIdAndCommunitiesId(targetUserId, communityId)) {
            throw new IllegalArgumentException("O usuário indicado não é membro desta comunidade");
        }
        if (communityAdminRepository.existsByCommunity_IdAndUser_Id(communityId, targetUserId)) {
            throw new IllegalArgumentException("Este usuário já é administrador");
        }
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        communityAdminRepository.save(CommunityAdmin.builder()
                .community(community)
                .user(targetUser)
                .build());
    }

    @Transactional
    public void leaveAdminRole(Long communityId, Long userId) {
        requireAdmin(communityId, userId);
        communityAdminRepository.deleteByCommunity_IdAndUser_Id(communityId, userId);
        ensureCommunityHasAdmin(communityId, userId, false);
    }

    @Transactional
    public CommunityResponse updateName(Long communityId, Long userId, UpdateCommunityRequest request) {
        requireAdmin(communityId, userId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));
        community.setName(request.getName().trim());
        communityRepository.save(community);
        Community withDetails = communityRepository.findByIdWithCreatedByAndMembers(communityId).orElse(community);
        return toResponseWithDetails(withDetails, userId);
    }

    @Transactional
    public CommunityResponse regenerateAccessCode(Long communityId, Long userId) {
        requireAdmin(communityId, userId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Condomínio não encontrado"));
        String newCode;
        do {
            newCode = generateAccessCode();
        } while (communityRepository.existsByAccessCode(newCode));
        community.setAccessCode(newCode);
        communityRepository.save(community);
        Community withDetails = communityRepository.findByIdWithCreatedByAndMembers(communityId).orElse(community);
        return toResponseWithDetails(withDetails, userId);
    }

    /**
     * When the only admin leaves (community or role), promote the most active member.
     * Most active = most ads in the community; tie-break by lower user id.
     */
    void ensureCommunityHasAdmin(Long communityId, Long excludeUserId, boolean excludeIsLeavingCommunity) {
        long adminCount = communityAdminRepository.countByCommunity_Id(communityId);
        if (adminCount > 0) {
            return;
        }
        Community community = communityRepository.findByIdWithCreatedByAndMembers(communityId).orElse(null);
        if (community == null) {
            return;
        }
        User nextAdmin = findMostActiveMember(community, excludeUserId, excludeIsLeavingCommunity);
        if (nextAdmin != null) {
            communityAdminRepository.save(CommunityAdmin.builder()
                    .community(community)
                    .user(nextAdmin)
                    .build());
        }
    }

    private User findMostActiveMember(Community community, Long excludeUserId, boolean excludeFromMembers) {
        List<User> candidates = community.getMembers().stream()
                .filter(m -> !m.getId().equals(excludeUserId))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.stream()
                .max(Comparator
                        .comparingLong((User u) -> adRepository.countByUserIdAndCommunityId(u.getId(), community.getId()))
                        .thenComparing((a, b) -> Long.compare(b.getId(), a.getId())))
                .orElse(null);
    }

    private String generateAccessCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(ACCESS_CODE_LENGTH);
        for (int i = 0; i < ACCESS_CODE_LENGTH; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String normalizePostalCode(String postalCode) {
        if (postalCode == null) return null;
        return postalCode.replaceAll("\\D", "");
    }

    private CommunityResponse toResponse(Community c, Long currentUserId) {
        boolean isAdmin = currentUserId != null && communityAdminRepository.existsByCommunity_IdAndUser_Id(c.getId(), currentUserId);
        return CommunityResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .accessCode(c.getAccessCode())
                .isPrivate(c.getIsPrivate())
                .postalCode(c.getPostalCode())
                .createdAt(c.getCreatedAt())
                .createdById(c.getCreatedBy().getId())
                .isAdmin(isAdmin)
                .build();
    }

    private CommunityResponse toResponseWithDetails(Community c, Long currentUserId) {
        List<String> memberNames = c.getMembers().stream()
                .map(User::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        List<MemberSummary> members = c.getMembers().stream()
                .sorted(Comparator.comparing(User::getName))
                .map(u -> MemberSummary.builder().id(u.getId()).name(u.getName()).build())
                .collect(Collectors.toList());
        boolean isAdmin = currentUserId != null && communityAdminRepository.existsByCommunity_IdAndUser_Id(c.getId(), currentUserId);
        List<Long> adminIds = null;
        if (isAdmin) {
            adminIds = communityAdminRepository.findByCommunity_Id(c.getId()).stream()
                    .map(ca -> ca.getUser().getId())
                    .sorted()
                    .collect(Collectors.toList());
        }
        return CommunityResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .accessCode(c.getAccessCode())
                .isPrivate(c.getIsPrivate())
                .postalCode(c.getPostalCode())
                .createdAt(c.getCreatedAt())
                .createdById(c.getCreatedBy().getId())
                .createdByName(c.getCreatedBy().getName())
                .memberNames(memberNames)
                .members(members)
                .isAdmin(isAdmin)
                .adminIds(adminIds)
                .build();
    }
}
