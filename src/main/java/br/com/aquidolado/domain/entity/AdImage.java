package br.com.aquidolado.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ad_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
