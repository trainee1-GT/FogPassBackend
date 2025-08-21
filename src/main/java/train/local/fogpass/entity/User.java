package train.local.fogpass.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Old fields
    @Column(name = "User_Id", nullable = false, length = 50)
    private String userId;

    @Column(name = "User_Id", nullable = false, length = 50)
    private String locoPilotId;

    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String username;


    @Column(name = "user_pwd", nullable = false, unique = true, length = 50)
    private String password;

    @Column(name = "Des", length = 100)
    private String designation;

    @Column(name = "Dept", length = 100)
    private String department;

    @Column(name = "BOD", length = 20)
    private String dateOfBirth;



    // Inverse side of one-to-one
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserSettings userSettings;

    // Inverse side of user-access-scopes
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserAccessScope> accessScopes = new HashSet<>();

    // Inverse side of journeys
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return password;
    }

    public void setPwd(String pwd) {
        this.password = pwd;
    }

    public String getDes() {
        return designation;
    }

    public void setDes(String des) {
        this.designation = des;
    }

    public String getDept() {
        return department;
    }

    public void setDept(String dept) {
        this.department = dept;
    }

    public String getBod() {
        return dateOfBirth;
    }

    public void setBod(String bod) {
        this.dateOfBirth = bod;
    }



    public String getLocoPilotId() {
        return locoPilotId;
    }

    public void setLocoPilotId(String locoPilotId) {
        this.locoPilotId = locoPilotId;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public Set<UserAccessScope> getAccessScopes() {
        return accessScopes;
    }

    public void setAccessScopes(Set<UserAccessScope> accessScopes) {
        this.accessScopes = accessScopes;
    }

    public Set<Journey> getJourneys() {
        return journeys;
    }

    public void setJourneys(Set<Journey> journeys) {
        this.journeys = journeys;
    }
}
