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
@Table(name = "zones")
public class Zone extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 10)
    private String code;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Division> divisions = new HashSet<>();
}