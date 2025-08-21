package train.local.fogpass.entity.logs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import train.local.fogpass.entity.Journey;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journey_logs", indexes = {
        @Index(name = "idx_journey_logs_journey_id", columnList = "journey_id"),
        @Index(name = "idx_journey_logs_log_timestamp", columnList = "log_timestamp")
})
public class JourneyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_timestamp")
    private LocalDateTime logTimestamp;

    private String eventType;

    @Column(columnDefinition = "json")
    private String eventData; // JSON as string

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    private Journey journey;
}