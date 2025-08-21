package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import train.local.fogpass.entity.enums.InitialModule;
import train.local.fogpass.entity.enums.JourneyStatus;
import train.local.fogpass.entity.logs.JourneyLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journeys", indexes = {
        @Index(name = "idx_journeys_user_id", columnList = "user_id"),
        @Index(name = "idx_journeys_device_id", columnList = "device_id"),
        @Index(name = "idx_journeys_route_id", columnList = "route_id"),
        @Index(name = "idx_journeys_start_time", columnList = "start_time")
})
public class Journey {

    private String trainNumber;
    private String trainCategory;
    private Integer locoCount;
    private Integer coachCount;
    private Integer authorizedSpeed;
    private BigDecimal trainMassTons;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InitialModule initialModule;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private JourneyStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @OneToOne(mappedBy = "journey", fetch = FetchType.LAZY)
    private PerformanceSummary performanceSummary;

    @OneToMany(mappedBy = "journey", fetch = FetchType.LAZY)
    private Set<JourneyLog> logs = new HashSet<>();
}