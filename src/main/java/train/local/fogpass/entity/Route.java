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
@Table(name = "routes", indexes = {
        @Index(name = "idx_routes_section_id", columnList = "section_id")
})
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String routeCode;

    @Column(nullable = false)
    private String name;

    private String direction;

    @Column(columnDefinition = "json")
    private String optimalProfileData; // JSON as string

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private Set<Landmark> landmarks = new HashSet<>();

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();
}