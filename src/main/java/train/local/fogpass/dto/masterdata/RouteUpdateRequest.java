package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "RouteUpdateRequest", description = "Partial update payload for Route")
public class RouteUpdateRequest {

    @Schema(description = "Route name", example = "Churchgate - Virar (Express)")
    private String name;

    @Schema(description = "Direction", example = "DOWN")
    private String direction;

    @Schema(description = "Optimal profile JSON data")
    private String optimalProfileData;
}