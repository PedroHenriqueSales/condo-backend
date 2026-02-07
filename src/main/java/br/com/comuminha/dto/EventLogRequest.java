package br.com.comuminha.dto;

import br.com.comuminha.domain.enums.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventLogRequest {

    @NotNull(message = "Tipo do evento é obrigatório")
    private EventType eventType;

    private Long communityId;
}
