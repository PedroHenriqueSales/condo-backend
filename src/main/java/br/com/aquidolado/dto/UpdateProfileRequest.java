package br.com.aquidolado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Telefone/WhatsApp é obrigatório")
    @Size(max = 50)
    private String whatsapp;

    @Size(max = 500)
    private String address;
}
