package br.com.aquidolado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String email;
    private String name;
    private String whatsapp;
    private String address;
    private Boolean emailVerified;
}
