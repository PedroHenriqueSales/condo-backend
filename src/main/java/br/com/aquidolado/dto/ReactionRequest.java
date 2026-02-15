package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.ReactionKind;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequest {

    @NotNull(message = "Tipo da reação é obrigatório (LIKE ou DISLIKE)")
    private ReactionKind kind;
}
