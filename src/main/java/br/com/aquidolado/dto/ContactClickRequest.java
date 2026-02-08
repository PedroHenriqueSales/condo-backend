package br.com.aquidolado.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactClickRequest {

    @NotNull(message = "ID do anúncio é obrigatório")
    private Long adId;

    @NotNull(message = "ID da comunidade é obrigatório")
    private Long communityId;
}
