package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import train.local.fogpass.entity.enums.PunctualityStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performance_summary")
public class PerformanceSummary {

    @Id
    private Long journeyId; // shared PK

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @Enumerated(EnumType.STRING)
    private PunctualityStatus punctualityStatus;

    private Integer totalDelayMinutes;

    private BigDecimal avgSpeed;

    private Integer totalAlerts;

    private BigDecimal efficiencyScore;

    @Column(columnDefinition = "TEXT")
    private String notes;
}