package train.local.fogpass.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Request body for assigning/replacing roles for a user.
 * This will REPLACE the user's existing roles with the provided set.
 */
public class AssignRolesRequest {

    @NotNull
    private Set<RoleAssignmentDto> roles = new HashSet<>();

    public Set<RoleAssignmentDto> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleAssignmentDto> roles) {
        this.roles = roles;
    }
}