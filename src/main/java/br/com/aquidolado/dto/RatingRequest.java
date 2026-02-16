package br.com.aquidolado.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RatingRequest {

    @Min(value = 0, message = "A nota deve ser entre 0 e 5")
    @Max(value = 5, message = "A nota deve ser entre 0 e 5")
    private int rating;
}
