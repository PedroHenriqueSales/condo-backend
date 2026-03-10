package br.com.aquidolado.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPatchRequest {

    private Boolean active;
    private Boolean emailVerified;
}
