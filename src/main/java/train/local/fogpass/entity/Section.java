package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import train.local.fogpass.entity.enums.SectionStatus;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sections", indexes = {
        @Index(name = "idx_sections_division_id", columnList = "division_id")
})
public class Section extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SectionStatus status = SectionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private Set<Route> routes = new HashSet<>();

    @Column(name = "updated_fields", length = 255)
    private String updatedFields;
}