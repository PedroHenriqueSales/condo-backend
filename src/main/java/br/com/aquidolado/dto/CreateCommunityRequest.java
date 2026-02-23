package br.com.aquidolado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommunityRequest {

    @NotBlank(message = "Nome do condomínio é obrigatório")
    @Size(max = 255)
    private String name;

    private boolean isPrivate;

    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 10)
    @Pattern(regexp = "^[0-9]{8}$|^[0-9]{5}-[0-9]{3}$", message = "CEP deve ter 8 dígitos (ex.: 12345678 ou 12345-678)")
    private String postalCode;
}
