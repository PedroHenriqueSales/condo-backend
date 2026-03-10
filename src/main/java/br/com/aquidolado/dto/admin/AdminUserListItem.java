package br.com.aquidolado.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListItem {

    private Long id;
    private String name;
    private String email;
    private Boolean active;
    private Boolean emailVerified;
    private Instant termsAcceptedAt;
}
