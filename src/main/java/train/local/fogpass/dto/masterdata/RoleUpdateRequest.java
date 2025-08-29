package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "RoleUpdateRequest", description = "Payload to update an existing role")
public class RoleUpdateRequest {

    @Size(min = 3, max = 50)
    @Schema(description = "New role name (optional)", example = "ADMIN")
    private String name;

    @Schema(description = "New description (optional)", example = "Administrators")
    private String description;
}