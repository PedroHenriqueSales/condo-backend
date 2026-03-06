package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.AccessCodeRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessCodeRequestResponse {

    private Long id;
    private Long userId;
    private String userName;
    private AccessCodeRequestStatus status;
    private Instant createdAt;
}
