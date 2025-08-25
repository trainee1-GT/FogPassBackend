package train.local.fogpass.dto.response;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class UserResponse {
    private Long id;
    private String username;
    private Long userId;
    private String locoPilotId;
    private LocalDate dateOfBirth;
    private String designation;
    private String department;
    private boolean active;
    private String mobNo;
    private Set<RoleScopeDto> scopes = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLocoPilotId() { return locoPilotId; }
    public void setLocoPilotId(String locoPilotId) { this.locoPilotId = locoPilotId; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getMobNo() { return mobNo; }
    public void setMobNo(String mobNo) { this.mobNo = mobNo; }
    public Set<RoleScopeDto> getScopes() { return scopes; }
    public void setScopes(Set<RoleScopeDto> scopes) { this.scopes = scopes; }
}