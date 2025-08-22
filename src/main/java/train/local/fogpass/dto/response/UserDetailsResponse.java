package train.local.fogpass.dto.response;

import java.time.LocalDate;

import train.local.fogpass.entity.User;

/**
 * DTO for returning user details in API responses.
 * Note: Password is intentionally excluded for security.
 */
public class UserDetailsResponse {
    private Long id;
    private String username;
    private String name;
    private String empId;
    private LocalDate dateOfBirth;
    private String designation;
    private String department;
    private boolean active;
    private String locoPilotId;

    public UserDetailsResponse() {
    }

    public UserDetailsResponse(Long id, String username, String name, String empId,
                               LocalDate dateOfBirth, String designation, String department,
                               boolean active, String locoPilotId) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.empId = empId;
        this.dateOfBirth = dateOfBirth;
        this.designation = designation;
        this.department = department;
        this.active = active;
        this.locoPilotId = locoPilotId;
    }

    public static UserDetailsResponse from(User user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmpId(),
                user.getDateOfBirth(),
                user.getDesignation(),
                user.getDepartment(),
                user.isActive(),
                user.getLocoPilotId()
        );
    }

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
}
