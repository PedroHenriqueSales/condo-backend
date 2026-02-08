package br.com.aquidolado.dto;

import br.com.aquidolado.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "ID do anúncio é obrigatório")
    private Long adId;

    @NotNull(message = "Motivo da denúncia é obrigatório")
    private ReportReason reason;
}
