package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.RecommendationReaction;
import br.com.aquidolado.domain.enums.ReactionKind;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.RecommendationReactionRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecommendationReactionService {

    private final AdRepository adRepository;
    private final RecommendationReactionRepository recommendationReactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void setReaction(Long adId, Long userId, ReactionKind kind) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Reações só são permitidas em indicações");
        }
        if (ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Quem criou a indicação não pode reagir");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        recommendationReactionRepository.findByAdIdAndUserId(adId, userId)
                .ifPresentOrElse(
                        r -> {
                            r.setKind(kind);
                            recommendationReactionRepository.save(r);
                        },
                        () -> recommendationReactionRepository.save(RecommendationReaction.builder()
                                .ad(ad)
                                .user(userRepository.getReferenceById(userId))
                                .kind(kind)
                                .createdAt(Instant.now())
                                .build())
                );
    }

    @Transactional
    public void removeReaction(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Reações só são permitidas em indicações");
        }
        if (ad.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Quem criou a indicação não pode reagir");
        }
        recommendationReactionRepository.deleteByAdIdAndUserId(adId, userId);
    }
}
