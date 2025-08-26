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
@Table(name = "divisions", indexes = {
        @Index(name = "idx_divisions_zone_id", columnList = "zone_id")
})
public class Division extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 10)
    private String code;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Section> sections = new HashSet<>();
}