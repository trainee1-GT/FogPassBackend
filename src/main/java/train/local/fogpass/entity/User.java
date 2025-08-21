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
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private String fullName;

    private String locoPilotId;

    // Inverse side of one-to-one
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserSettings userSettings;

    // Inverse side of user-access-scopes
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserAccessScope> accessScopes = new HashSet<>();

    // Inverse side of journeys
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();
}