package train.local.fogpass.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sections", indexes = {
        @Index(name = "idx_sections_division_id", columnList = "division_id")
})
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    @JsonIgnore // avoid back-reference causing recursion
    private Division division;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private Set<Route> routes = new HashSet<>();

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

    // Support flat divisionId in JSON while keeping JPA relation
    @com.fasterxml.jackson.annotation.JsonProperty("divisionId")
    public Long getDivisionId() {
        return division != null ? division.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("divisionId")
    public void setDivisionId(Long divisionId) {
        if (divisionId == null) {
            this.division = null;
        } else {
            if (this.division == null) this.division = new Division();
            this.division.setId(divisionId);
        }
    }
}