package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dados mínimos do anúncio para montar preview (Open Graph) em compartilhamento.
 * imagePath é o path da primeira imagem (ex.: /uploads/ads/1/xxx.jpg) ou null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdOgResponse {
    private String title;
    private String imagePath;
}
