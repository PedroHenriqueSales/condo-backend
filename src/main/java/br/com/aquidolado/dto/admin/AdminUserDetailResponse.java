package br.com.aquidolado.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {

    private Long id;
    private String name;
    private String email;
    private String whatsapp;
    private Boolean active;
    private Boolean emailVerified;
    private Boolean systemAdmin;
    private Instant termsAcceptedAt;
    private List<Long> communityIds;
}
