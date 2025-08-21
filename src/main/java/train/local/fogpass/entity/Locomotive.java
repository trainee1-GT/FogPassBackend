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
@Table(name = "locomotives", indexes = {
        @Index(name = "idx_locomotives_loco_number", columnList = "locoNumber", unique = true)
})
public class Locomotive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String locoNumber;

    @OneToMany(mappedBy = "locomotive", fetch = FetchType.LAZY)
    private Set<Device> devices = new HashSet<>();
}