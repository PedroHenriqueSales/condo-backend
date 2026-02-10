package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.dto.CommunityResponse;
import br.com.aquidolado.dto.CreateCommunityRequest;
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

    @Transactional
    public CommunityResponse create(Long userId, CreateCommunityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String accessCode = generateAccessCode();

        Community community = Community.builder()
                .name(request.getName())
                .accessCode(accessCode)
                .createdAt(Instant.now())
                .createdBy(user)
                .build();

        community = communityRepository.save(community);
        user.getCommunities().add(community);
        userRepository.save(user);

        return toResponse(community);
    }

    @Transactional
    public CommunityResponse joinByAccessCode(Long userId, String accessCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Community community = communityRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new IllegalArgumentException("Código de acesso inválido"));

        if (userRepository.existsByIdAndCommunitiesId(userId, community.getId())) {
            throw new IllegalArgumentException("Você já é membro deste condomínio");
        }

        user.getCommunities().add(community);
        userRepository.save(user);

        return toResponse(community);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> listUserCommunities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        return user.getCommunities().stream()
                .map(this::toResponse)
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

        return toResponseWithDetails(community);
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

        userRepository.save(user);
    }

    private String generateAccessCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(ACCESS_CODE_LENGTH);
        for (int i = 0; i < ACCESS_CODE_LENGTH; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private CommunityResponse toResponse(Community c) {
        return CommunityResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .accessCode(c.getAccessCode())
                .createdAt(c.getCreatedAt())
                .createdById(c.getCreatedBy().getId())
                .build();
    }

    private CommunityResponse toResponseWithDetails(Community c) {
        List<String> memberNames = c.getMembers().stream()
                .map(User::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        return CommunityResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .accessCode(c.getAccessCode())
                .createdAt(c.getCreatedAt())
                .createdById(c.getCreatedBy().getId())
                .createdByName(c.getCreatedBy().getName())
                .memberNames(memberNames)
                .build();
    }
}
