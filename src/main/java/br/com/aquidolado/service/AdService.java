package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.AdImage;
import br.com.aquidolado.domain.entity.Community;
import br.com.aquidolado.domain.entity.RecommendationReaction;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.AdType;
import br.com.aquidolado.domain.enums.EventType;
import br.com.aquidolado.dto.AdResponse;
import br.com.aquidolado.dto.CreateAdRequest;
import br.com.aquidolado.dto.UpdateAdRequest;
import br.com.aquidolado.repository.AdImageRepository;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.CommunityRepository;
import br.com.aquidolado.repository.RecommendationReactionRepository;
import br.com.aquidolado.repository.UserRepository;
import br.com.aquidolado.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private static final int MAX_IMAGES = 5;

    private final AdRepository adRepository;
    private final AdImageRepository adImageRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final RecommendationReactionRepository recommendationReactionRepository;
    private final EventLogService eventLogService;
    private final StorageService storageService;

    @Transactional
    public AdResponse create(Long userId, CreateAdRequest request, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));

        if (!userRepository.existsByIdAndCommunitiesId(userId, community.getId())) {
            throw new IllegalArgumentException("Você não pertence a esta comunidade");
        }

        if (request.getType() == AdType.RECOMMENDATION) {
            if (images != null && !images.stream().allMatch(f -> f == null || f.isEmpty())) {
                throw new IllegalArgumentException("Indicações não podem ter fotos");
            }
            if (request.getRecommendedContact() == null || request.getRecommendedContact().isBlank()) {
                throw new IllegalArgumentException("Contato do indicado é obrigatório para indicações");
            }
            if (request.getServiceType() == null || request.getServiceType().isBlank()) {
                throw new IllegalArgumentException("Tipo de serviço é obrigatório para indicações");
            }
        }

        BigDecimal priceToSave = (request.getType() == AdType.DONATION || request.getType() == AdType.RECOMMENDATION)
                ? null : request.getPrice();
        Ad ad = Ad.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .price(priceToSave)
                .status(AdStatus.ACTIVE)
                .user(user)
                .community(community)
                .createdAt(Instant.now())
                .recommendedContact(request.getType() == AdType.RECOMMENDATION ? request.getRecommendedContact().trim() : null)
                .serviceType(request.getType() == AdType.RECOMMENDATION ? request.getServiceType().trim() : null)
                .build();

        ad = adRepository.save(ad);

        if (request.getType() != AdType.RECOMMENDATION) {
            saveImages(ad, images);
        }

        eventLogService.log(EventType.CREATE_AD, userId, community.getId());

        return toResponse(ad, userId);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> listByCommunity(Long communityId, Long userId, List<AdType> types, String search, Pageable pageable) {
        validateUserInCommunity(userId, communityId);

        // Monta o padrão em Java para evitar LOWER(bytea) no PostgreSQL
        String searchPattern = (search != null && !search.isBlank())
                ? "%" + search.trim().toLowerCase() + "%"
                : null;

        // Se a lista estiver vazia, passa null para retornar todos os tipos
        List<AdType> typesToUse = (types != null && !types.isEmpty()) ? types : null;

        Page<Ad> ads = adRepository.findByCommunityWithFilters(
                communityId, AdStatus.ACTIVE, typesToUse, searchPattern, pageable);

        return ads.map(ad -> toResponse(ad, userId));
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> listMyAds(Long userId, Long communityId, Pageable pageable) {
        if (communityId != null) {
            return adRepository.findByUserIdAndCommunityIdWithUser(userId, communityId, pageable)
                    .map(ad -> toResponse(ad, userId));
        }
        return adRepository.findByUserIdWithUser(userId, pageable).map(ad -> toResponse(ad, userId));
    }

    @Transactional
    public AdResponse update(Long adId, Long userId, UpdateAdRequest request, List<MultipartFile> newImages) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode editar este anúncio");
        }

        if (ad.getStatus() == AdStatus.CLOSED) {
            throw new IllegalArgumentException("Não é possível editar anúncios encerrados");
        }
        if (ad.getStatus() == AdStatus.REMOVED) {
            throw new IllegalArgumentException("Anúncio removido por denúncias");
        }

        if (request.getType() == AdType.RECOMMENDATION) {
            if (newImages != null && !newImages.stream().allMatch(f -> f == null || f.isEmpty())) {
                throw new IllegalArgumentException("Indicações não podem ter fotos");
            }
            if (request.getRecommendedContact() == null || request.getRecommendedContact().isBlank()) {
                throw new IllegalArgumentException("Contato do indicado é obrigatório para indicações");
            }
            if (request.getServiceType() == null || request.getServiceType().isBlank()) {
                throw new IllegalArgumentException("Tipo de serviço é obrigatório para indicações");
            }
        }

        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setType(request.getType());
        ad.setPrice((request.getType() == AdType.DONATION || request.getType() == AdType.RECOMMENDATION) ? null : request.getPrice());
        ad.setRecommendedContact(request.getType() == AdType.RECOMMENDATION ? request.getRecommendedContact().trim() : null);
        ad.setServiceType(request.getType() == AdType.RECOMMENDATION ? request.getServiceType().trim() : null);
        ad = adRepository.save(ad);

        if (ad.getType() != AdType.RECOMMENDATION && newImages != null && !newImages.isEmpty()) {
            List<AdImage> existing = adImageRepository.findByAdIdOrderBySortOrder(adId);
            for (AdImage img : existing) {
                storageService.delete(img.getUrl());
            }
            adImageRepository.deleteByAdId(adId);
            saveImages(ad, newImages);
        }

        return toResponse(ad, userId);
    }

    @Transactional
    public AdResponse pauseAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode pausar este anúncio");
        }

        if (ad.getStatus() != AdStatus.ACTIVE) {
            throw new IllegalArgumentException("Só é possível pausar anúncios ativos");
        }

        ad.setStatus(AdStatus.PAUSED);
        ad = adRepository.save(ad);

        return toResponse(ad, userId);
    }

    @Transactional
    public AdResponse unpauseAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode reativar este anúncio");
        }

        if (ad.getStatus() != AdStatus.PAUSED) {
            throw new IllegalArgumentException("Só é possível reativar anúncios pausados");
        }

        if (ad.getSuspendedByReportsAt() != null) {
            throw new IllegalArgumentException("Anúncio suspenso por denúncias. Entre em contato para solicitar revisão.");
        }

        ad.setStatus(AdStatus.ACTIVE);
        ad = adRepository.save(ad);

        return toResponse(ad, userId);
    }

    @Transactional
    public AdResponse closeAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode encerrar este anúncio");
        }
        if (ad.getStatus() == AdStatus.REMOVED) {
            throw new IllegalArgumentException("Anúncio removido por denúncias");
        }

        ad.setStatus(AdStatus.CLOSED);
        ad = adRepository.save(ad);

        return toResponse(ad, userId);
    }

    @Transactional
    public void deleteAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode remover este anúncio");
        }

        if (ad.getStatus() != AdStatus.CLOSED) {
            throw new IllegalArgumentException("Só é possível remover anúncios encerrados");
        }

        storageService.deleteByPrefix("ads/" + adId);
        adRepository.delete(ad);
    }

    @Transactional(readOnly = true)
    public AdResponse getById(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        validateUserInCommunity(userId, ad.getCommunity().getId());

        if (ad.getStatus() == AdStatus.REMOVED && !ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Anúncio não encontrado");
        }

        return toResponse(ad, userId);
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

    private void saveImages(Ad ad, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        List<MultipartFile> toSave = images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .limit(MAX_IMAGES)
                .toList();
        if (toSave.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Máximo de " + MAX_IMAGES + " imagens por anúncio");
        }
        String prefix = "ads/" + ad.getId();
        for (int i = 0; i < toSave.size(); i++) {
            String url = storageService.save(toSave.get(i), prefix);
            AdImage img = AdImage.builder()
                    .ad(ad)
                    .url(url)
                    .sortOrder(i)
                    .build();
            adImageRepository.save(img);
        }
    }

    private AdResponse toResponse(Ad ad, Long currentUserId) {
        List<String> urls = ad.getType() == AdType.RECOMMENDATION
                ? List.of()
                : adImageRepository.findByAdIdOrderBySortOrder(ad.getId())
                        .stream()
                        .map(AdImage::getUrl)
                        .toList();
        AdResponse.AdResponseBuilder builder = AdResponse.builder()
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
                .imageUrls(urls)
                .recommendedContact(ad.getRecommendedContact())
                .serviceType(ad.getServiceType())
                .suspendedByReportsAt(ad.getSuspendedByReportsAt());
        if (ad.getType() == AdType.RECOMMENDATION) {
            long ratingCount = recommendationReactionRepository.countByAdId(ad.getId());
            builder.ratingCount(ratingCount);
            builder.averageRating(ratingCount > 0 ? recommendationReactionRepository.getAverageRatingByAdId(ad.getId()) : null);
            if (currentUserId != null) {
                builder.currentUserRating(
                        recommendationReactionRepository.findByAdIdAndUserId(ad.getId(), currentUserId)
                                .map(RecommendationReaction::getRating)
                                .orElse(null));
            }
        }
        return builder.build();
    }
}
