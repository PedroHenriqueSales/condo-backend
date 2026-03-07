package br.com.aquidolado.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommunityRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    private String name;

    @JsonProperty("isPrivate")
    private Boolean isPrivate;

    @DecimalMin(value = "-90", message = "Latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90", message = "Latitude deve estar entre -90 e 90")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180", message = "Longitude deve estar entre -180 e 180")
    private Double longitude;
}
