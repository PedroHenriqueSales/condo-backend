package br.com.aquidolado.dto.admin;

import br.com.aquidolado.domain.enums.AdStatus;
import br.com.aquidolado.domain.enums.AdType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAdListItem {

    private Long id;
    private String title;
    private AdType type;
    private AdStatus status;
    private Long userId;
    private String userName;
    private Long communityId;
    private String communityName;
    private Instant createdAt;
}
