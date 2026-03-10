package br.com.aquidolado.dto.admin;

import br.com.aquidolado.domain.enums.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportListItem {

    private Long id;
    private Long adId;
    private String adTitle;
    private ReportReason reason;
    private Long reporterUserId;
    private String reporterUserName;
    private Long communityId;
    private String communityName;
    private Instant createdAt;
}
