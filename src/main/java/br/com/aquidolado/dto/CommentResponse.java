package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long adId;
    private Long userId;
    private String userName;
    private String text;
    private Instant createdAt;
    private long likeCount;
    private boolean currentUserLiked;
}
