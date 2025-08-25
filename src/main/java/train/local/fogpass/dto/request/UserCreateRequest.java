package train.local.fogpass.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class UserCreateRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    private Long userId;
    private String locoPilotId;
    private LocalDate dateOfBirth;
    private String designation;
    private String department;
    private boolean active = true;
    private String mobNo;

    private Set<RoleAssignmentDto> roles = new HashSet<>();

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
    public Set<RoleAssignmentDto> getRoles() { return roles; }
    public void setRoles(Set<RoleAssignmentDto> roles) { this.roles = roles; }
}