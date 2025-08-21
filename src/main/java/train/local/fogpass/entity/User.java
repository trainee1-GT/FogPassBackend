package train.local.fogpass.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_emp_id", columnList = "empId", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

<<<<<<< HEAD
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

    @Column(name = "DOB", length = 20)
    private String dateOfBirth;



    // Inverse side of one-to-one
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
=======
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String empId;

    private LocalDate dateOfBirth;

    private String designation;

    private String department;

    @Column(nullable = false)
    private boolean active = true;

    private String locoPilotId;

    // --- Relationships ---
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
>>>>>>> kunal
    private UserSettings userSettings;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserAccessScope> accessScopes = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Journey> journeys = new HashSet<>();

    // --- Constructors ---

    /**
     * Default no-argument constructor required by JPA.
     */
    public User() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

<<<<<<< HEAD
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
=======
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
>>>>>>> kunal
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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