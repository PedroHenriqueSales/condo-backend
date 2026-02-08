package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.AdType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdResponse {

    private Long id;
    private String title;
    private String description;
    private AdType type;
    private BigDecimal price;
    private AdStatus status;
    private Long userId;
    private String userName;
    private String userWhatsapp;
    private Long communityId;
    private Instant createdAt;
    private List<String> imageUrls;
}
