package train.local.fogpass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
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

    // Auditing fields
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private Instant createdDate;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    @LastModifiedDate
    @Column(name = "updated_date")
    private Instant updatedDate;

    @OneToMany(mappedBy = "division", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Section> sections = new HashSet<>();

    // Explicit getters to ensure JSON serialization without Lombok
    public Long getId() { return id; }
    public String getName() { return name; }
    public Zone getZone() { return zone; }
    public Set<Section> getSections() { return sections; }

    // JSON alias for id to expose `divisionId` in API responses without DB changes
    @com.fasterxml.jackson.annotation.JsonProperty("divisionId")
    public Long getDivisionId() { return id; }

    // Support `divisionName` in JSON (serialize and deserialize)
    @com.fasterxml.jackson.annotation.JsonProperty("divisionName")
    public String getDivisionName() { return name; }
    public void setDivisionName(String divisionName) { this.name = divisionName; }

    // Support flat `zoneId` in JSON while keeping JPA relation
    @com.fasterxml.jackson.annotation.JsonProperty("zoneId")
    public Long getZoneId() { return zone != null ? zone.getZoneId() : null; }
    @com.fasterxml.jackson.annotation.JsonProperty("zoneId")
    public void setZoneId(Long zoneId) {
        if (zoneId == null) {
            this.zone = null;
        } else {
            if (this.zone == null) this.zone = new Zone();
            this.zone.setZoneId(zoneId);
        }
    }

    // Explicit setters to avoid Lombok/annotation processing issues during updates
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setZone(Zone zone) { this.zone = zone; }

}