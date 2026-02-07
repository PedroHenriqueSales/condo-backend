package br.com.comuminha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommunityRequest {

    @NotBlank(message = "Nome do condomínio é obrigatório")
    @Size(max = 255)
    private String name;
}
