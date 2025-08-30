package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "RouteCreateRequest", description = "Payload to create a Route")
public class RouteCreateRequest {

    @NotBlank
    @Schema(description = "Route name", example = "Churchgate - Virar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Schema(description = "Unique route code", example = "WR-MUM-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String routeCode;

    @Schema(description = "Direction", example = "UP")
    private String direction;

    @NotNull
    @Schema(description = "Owning Section ID", example = "55", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectionId;

    @Schema(description = "Optimal profile JSON data")
    private String optimalProfileData;
}