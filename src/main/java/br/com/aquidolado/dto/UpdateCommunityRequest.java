package br.com.aquidolado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommunityRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;
}
