package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {

    private Long id;
    private String name;
    private String accessCode;
    private Instant createdAt;
    private Long createdById;
    /** Preenchido apenas no detalhe (GET /communities/{id}) */
    private String createdByName;
    /** Preenchido apenas no detalhe (GET /communities/{id}), nomes ordenados */
    private List<String> memberNames;
}
