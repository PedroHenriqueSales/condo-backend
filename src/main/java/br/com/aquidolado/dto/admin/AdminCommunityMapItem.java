package br.com.aquidolado.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommunityMapItem {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Long membersCount;
}
