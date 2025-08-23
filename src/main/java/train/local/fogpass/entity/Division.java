package train.local.fogpass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sections"})
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Division name is required")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"divisions", "hibernateLazyInitializer", "handler"})
    private Zone zone;

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Section> sections = new HashSet<>();

    // Explicit getters to ensure JSON serialization without Lombok
    public Long getId() { return id; }
    public String getName() { return name; }
    public Zone getZone() { return zone; }
    public Set<Section> getSections() { return sections; }

    // Explicit setters to avoid Lombok/annotation processing issues during updates
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setZone(Zone zone) { this.zone = zone; }
}