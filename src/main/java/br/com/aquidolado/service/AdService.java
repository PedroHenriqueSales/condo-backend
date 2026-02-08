package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.AdImage;
import br.com.aquidolado.domain.entity.Community;
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
import br.com.aquidolado.repository.UserRepository;
import br.com.aquidolado.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private static final int MAX_IMAGES = 3;

    private final AdRepository adRepository;
    private final AdImageRepository adImageRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
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

        saveImages(ad, images);

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
    public AdResponse update(Long adId, Long userId, UpdateAdRequest request, List<MultipartFile> newImages) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));

        if (!ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode editar este anúncio");
        }

        if (ad.getStatus() == AdStatus.CLOSED) {
            throw new IllegalArgumentException("Não é possível editar anúncios encerrados");
        }

        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setType(request.getType());
        ad.setPrice(request.getPrice());
        ad = adRepository.save(ad);

        if (newImages != null && !newImages.isEmpty()) {
            List<AdImage> existing = adImageRepository.findByAdIdOrderBySortOrder(adId);
            for (AdImage img : existing) {
                storageService.delete(img.getUrl());
            }
            adImageRepository.deleteByAdId(adId);
            saveImages(ad, newImages);
        }

        return toResponse(ad);
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

        return toResponse(ad);
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

        ad.setStatus(AdStatus.ACTIVE);
        ad = adRepository.save(ad);

        return toResponse(ad);
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

    private AdResponse toResponse(Ad ad) {
        List<String> urls = adImageRepository.findByAdIdOrderBySortOrder(ad.getId())
                .stream()
                .map(AdImage::getUrl)
                .toList();
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
                .imageUrls(urls)
                .build();
    }
}
