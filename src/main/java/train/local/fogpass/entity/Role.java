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
@Table(name = "roles", indexes = {
        @Index(name = "idx_roles_name", columnList = "name", unique = true)
})
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private train.local.fogpass.entity.enums.RoleStatus status = train.local.fogpass.entity.enums.RoleStatus.ACTIVE;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserAccessScope> userAccessScopes = new HashSet<>();
}