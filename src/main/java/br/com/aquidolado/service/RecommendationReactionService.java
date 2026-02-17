package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.RecommendationReaction;
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
    public void setRating(Long adId, Long userId, int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("A nota deve ser entre 0 e 5");
        }
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Avaliações só são permitidas em indicações");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        recommendationReactionRepository.findByAdIdAndUserId(adId, userId)
                .ifPresentOrElse(
                        r -> {
                            r.setRating(rating);
                            recommendationReactionRepository.save(r);
                        },
                        () -> recommendationReactionRepository.save(RecommendationReaction.builder()
                                .ad(ad)
                                .user(userRepository.getReferenceById(userId))
                                .rating(rating)
                                .createdAt(Instant.now())
                                .build())
                );
    }

    @Transactional
    public void removeReaction(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Avaliações só são permitidas em indicações");
        }
        recommendationReactionRepository.deleteByAdIdAndUserId(adId, userId);
    }
}
