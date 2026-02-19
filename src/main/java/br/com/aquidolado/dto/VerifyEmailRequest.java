package br.com.aquidolado.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {

    @NotBlank(message = "Token é obrigatório")
    private String token;
}
