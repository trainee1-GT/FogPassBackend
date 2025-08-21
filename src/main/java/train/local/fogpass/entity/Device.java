package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_devices_locomotive_id", columnList = "locomotive_id")
})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceCode;

    @Column(nullable = false)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locomotive_id")
    private Locomotive locomotive;

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();
}