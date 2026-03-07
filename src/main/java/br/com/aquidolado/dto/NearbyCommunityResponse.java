package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyCommunityResponse {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    /** Distância em km do ponto de busca (preenchido na listagem por proximidade). */
    private Double distanceKm;
}
