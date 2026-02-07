package br.com.comuminha.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinCommunityRequest {

    @NotBlank(message = "Código de acesso é obrigatório")
    private String accessCode;
}
