package br.com.aquidolado.service;

import br.com.aquidolado.domain.entity.CommentLike;
import br.com.aquidolado.domain.entity.RecommendationComment;
import br.com.aquidolado.domain.entity.Ad;
import br.com.aquidolado.domain.entity.User;
import br.com.aquidolado.dto.CommentResponse;
import br.com.aquidolado.dto.CreateCommentRequest;
import br.com.aquidolado.repository.AdRepository;
import br.com.aquidolado.repository.CommentLikeRepository;
import br.com.aquidolado.repository.RecommendationCommentRepository;
import br.com.aquidolado.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecommendationCommentService {

    private final AdRepository adRepository;
    private final RecommendationCommentRepository recommendationCommentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long adId, Long currentUserId, Pageable pageable) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Comentários só são permitidos em indicações");
        }
        if (!userRepository.existsByIdAndCommunitiesId(currentUserId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        return recommendationCommentRepository.findByAdIdOrderByCreatedAtAsc(adId, pageable)
                .map(c -> toCommentResponse(c, currentUserId));
    }

    @Transactional
    public CommentResponse createComment(Long adId, Long userId, CreateCommentRequest request) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Comentários só são permitidos em indicações");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        RecommendationComment comment = RecommendationComment.builder()
                .ad(ad)
                .user(user)
                .text(request.getText().trim())
                .createdAt(Instant.now())
                .build();
        comment = recommendationCommentRepository.save(comment);
        return toCommentResponse(comment, userId);
    }

    @Transactional
    public void toggleCommentLike(Long adId, Long commentId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Curtir comentário só é permitido em indicações");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        RecommendationComment comment = recommendationCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));
        if (!comment.getAd().getId().equals(adId)) {
            throw new IllegalArgumentException("Comentário não pertence a esta indicação");
        }
        if (comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Você não pode curtir seu próprio comentário");
        }
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        } else {
            commentLikeRepository.save(CommentLike.builder()
                    .comment(comment)
                    .user(userRepository.getReferenceById(userId))
                    .createdAt(Instant.now())
                    .build());
        }
    }

    @Transactional
    public void deleteComment(Long adId, Long commentId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Anúncio não encontrado"));
        if (ad.getType() != br.com.aquidolado.domain.enums.AdType.RECOMMENDATION) {
            throw new IllegalArgumentException("Comentários só existem em indicações");
        }
        if (!userRepository.existsByIdAndCommunitiesId(userId, ad.getCommunity().getId())) {
            throw new IllegalArgumentException("Você não tem acesso a esta comunidade");
        }
        RecommendationComment comment = recommendationCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado"));
        if (!comment.getAd().getId().equals(adId)) {
            throw new IllegalArgumentException("Comentário não pertence a esta indicação");
        }
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Somente quem publicou pode apagar o comentário");
        }
        commentLikeRepository.deleteByCommentId(commentId);
        recommendationCommentRepository.delete(comment);
    }

    private CommentResponse toCommentResponse(RecommendationComment c, Long currentUserId) {
        long likeCount = commentLikeRepository.countByCommentId(c.getId());
        boolean currentUserLiked = currentUserId != null && commentLikeRepository.existsByCommentIdAndUserId(c.getId(), currentUserId);
        return CommentResponse.builder()
                .id(c.getId())
                .adId(c.getAd().getId())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .text(c.getText())
                .createdAt(c.getCreatedAt())
                .likeCount(likeCount)
                .currentUserLiked(currentUserLiked)
                .build();
    }
}
