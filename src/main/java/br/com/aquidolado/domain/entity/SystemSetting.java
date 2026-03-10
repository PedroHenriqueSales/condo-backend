package br.com.aquidolado.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @Column(name = "setting_key", length = 255)
    private String key;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;
}
