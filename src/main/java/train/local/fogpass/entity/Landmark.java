package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "landmarks", indexes = {
        @Index(name = "idx_landmarks_route_id", columnList = "route_id"),
        @Index(name = "idx_landmarks_route_seq", columnList = "route_id, sequence_order")
})
public class Landmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    private String locationCode;

    private String landmarkType;

    private String name;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer prewarningDistance;

    private String direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;
}