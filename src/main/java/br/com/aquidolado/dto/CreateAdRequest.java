package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.AdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAdRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Tipo do anúncio é obrigatório")
    private AdType type;

    private BigDecimal price;

    @NotNull(message = "ID da comunidade é obrigatório")
    private Long communityId;
}
