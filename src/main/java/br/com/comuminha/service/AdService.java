package br.com.comuminha.service;

import br.com.comuminha.domain.entity.Ad;
import br.com.comuminha.domain.entity.Community;
import br.com.comuminha.domain.entity.User;
import br.com.comuminha.domain.enums.AdStatus;
import br.com.comuminha.domain.enums.AdType;
import br.com.comuminha.domain.enums.EventType;
import br.com.comuminha.dto.AdResponse;
import br.com.comuminha.dto.CreateAdRequest;
import br.com.comuminha.repository.AdRepository;
import br.com.comuminha.repository.CommunityRepository;
import br.com.comuminha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final EventLogService eventLogService;

    @Transactional
    public AdResponse create(Long userId, CreateAdRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));

        if (!userRepository.existsByIdAndCommunitiesId(userId, community.getId())) {
            throw new IllegalArgumentException("Você não pertence a esta comunidade");
        }

        Ad ad = Ad.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .price(request.getPrice())
                .status(AdStatus.ACTIVE)
                .user(user)
                .community(community)
                .createdAt(Instant.now())
                .build();

        ad = adRepository.save(ad);

        eventLogService.log(EventType.CREATE_AD, userId, community.getId());

        return toResponse(ad);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> listByCommunity(Long communityId, Long userId, AdType type, String search, Pageable pageable) {
        validateUserInCommunity(userId, communityId);

        // Monta o padrão em Java para evitar LOWER(bytea) no PostgreSQL
        String searchPattern = (search != null && !search.isBlank())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;

        Page<Ad> ads = adRepository.findByCommunityWithFilters(
                communityId, AdStatus.ACTIVE, type, searchPattern, pageable);

        return ads.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> listMyAds(Long userId, Pageable pageable) {
        return adRepository.findByUserIdWithUser(userId, pageable).map(this::toResponse);
    }

    @Transactional
    public AdResponse closeAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode encerrar este anúncio");
        }

        ad.setStatus(AdStatus.CLOSED);
        ad = adRepository.save(ad);

        return toResponse(ad);
    }

    @Transactional(readOnly = true)
    public AdResponse getById(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        validateUserInCommunity(userId, ad.getCommunity().getId());

        return toResponse(ad);
    }

    private void validateUserInCommunity(Long userId, Long communityId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (!communityRepository.existsById(communityId)) {
            throw new IllegalArgumentException("Comunidade não encontrada");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, communityId)) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
    }

    private AdResponse toResponse(Ad ad) {
        return AdResponse.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .description(ad.getDescription())
                .type(ad.getType())
                .price(ad.getPrice())
                .status(ad.getStatus())
                .userId(ad.getUser().getId())
                .userName(ad.getUser().getName())
                .userWhatsapp(ad.getUser().getWhatsapp())
                .communityId(ad.getCommunity().getId())
                .createdAt(ad.getCreatedAt())
                .build();
    }
}
