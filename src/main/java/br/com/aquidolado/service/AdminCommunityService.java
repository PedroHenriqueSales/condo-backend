package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.dto.admin.AdminCommunityListItem;
import br.com.aquidolado.dto.admin.AdminCommunityMapItem;
import br.com.aquidolado.dto.MemberSummary;
import br.com.aquidolado.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityService communityService;
    private final AdminAuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<AdminCommunityListItem> listCommunities(Pageable pageable) {
        return communityRepository.findAll(pageable).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public AdminCommunityListItem getCommunity(Long id) {
        Community c = communityRepository.findByIdWithCreatedByAndMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));
        return toListItemWithMembers(c);
    }

    @Transactional(readOnly = true)
    public java.util.List<MemberSummary> getMembers(Long communityId) {
        Community c = communityRepository.findByIdWithCreatedByAndMembers(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));
        return c.getMembers().stream()
                .map(u -> MemberSummary.builder().id(u.getId()).name(u.getName()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCommunity(Long communityId, Long adminUserId) {
        Community c = communityRepository.findById(communityId).orElse(null);
        String name = c != null ? c.getName() : String.valueOf(communityId);
        communityService.deleteCommunityAsSystemAdmin(communityId);
        auditLogService.log(adminUserId, "DELETE_COMMUNITY", "Community", String.valueOf(communityId), "name=" + name);
    }

    @Transactional(readOnly = true)
    public java.util.List<AdminCommunityMapItem> getMapItems() {
        return communityRepository.findAll().stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(c -> AdminCommunityMapItem.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .latitude(c.getLatitude())
                        .longitude(c.getLongitude())
                        .membersCount(c.getMembers() != null ? (long) c.getMembers().size() : 0L)
                        .build())
                .collect(Collectors.toList());
    }

    private AdminCommunityListItem toListItem(Community c) {
        int size = c.getMembers() != null ? c.getMembers().size() : 0;
        return AdminCommunityListItem.builder()
                .id(c.getId())
                .name(c.getName())
                .accessCode(c.getAccessCode())
                .isPrivate(c.getIsPrivate())
                .membersCount((long) size)
                .createdAt(c.getCreatedAt())
                .createdById(c.getCreatedBy() != null ? c.getCreatedBy().getId() : null)
                .createdByName(c.getCreatedBy() != null ? c.getCreatedBy().getName() : null)
                .build();
    }

    private AdminCommunityListItem toListItemWithMembers(Community c) {
        AdminCommunityListItem item = toListItem(c);
        if (c.getMembers() != null) {
            item.setMembersCount((long) c.getMembers().size());
        }
        return item;
    }
}
