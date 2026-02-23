package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.JoinRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponse {
    private Long id;
    private Long userId;
    private String userName;
    private JoinRequestStatus status;
    private Instant createdAt;
}
