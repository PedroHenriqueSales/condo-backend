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
public class AdminCommunityListItem {

    private Long id;
    private String name;
    private String accessCode;
    private Boolean isPrivate;
    private Long membersCount;
    private Instant createdAt;
    private Long createdById;
    private String createdByName;
}
