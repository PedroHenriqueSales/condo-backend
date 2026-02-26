package br.com.aquidolado.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100)
    private String password;

    @Size(max = 50)
    private String whatsapp;

    @Size(max = 500)
    private String address;

    @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade")
    private Boolean acceptTerms;

    @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade")
    private Boolean acceptPrivacy;
}
