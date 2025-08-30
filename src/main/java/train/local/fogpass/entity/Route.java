package train.local.fogpass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import train.local.fogpass.entity.enums.RouteStatus;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "routes",
        indexes = {
                @Index(name = "idx_routes_section_id", columnList = "section_id"),
                @Index(name = "idx_routes_code", columnList = "route_code", unique = true)
        })
public class Route extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "route_code", nullable = false, unique = true)
    private String routeCode;

    @Column(name = "direction")
    private String direction;

    @Column(name = "optimal_profile_data", columnDefinition = "json")
    private String optimalProfileData; // JSON as string

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RouteStatus status = RouteStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @NotNull
    private Section section;

    // Existing relationships retained
    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private Set<Landmark> landmarks = new HashSet<>();

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();
}