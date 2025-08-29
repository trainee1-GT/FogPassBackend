package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "RoleCreateRequest", description = "Payload to create a new role")
public class RoleCreateRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Unique role name", example = "SUPER_ADMIN")
    private String name;

    @Schema(description = "Optional description", example = "Super administrators")
    private String description;
}