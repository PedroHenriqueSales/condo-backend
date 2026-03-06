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
    private String postalCode;
}
