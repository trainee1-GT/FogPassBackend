package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "RouteResponse", description = "Route details with audit fields")
public class RouteResponse {

    @Schema(description = "Primary identifier", example = "101")
    private Long id;

    @Schema(description = "Route name", example = "Churchgate - Virar")
    private String name;

    @Schema(description = "Unique route code", example = "WR-MUM-001")
    private String routeCode;

    @Schema(description = "Direction", example = "UP")
    private String direction;

    @Schema(description = "Optimal profile JSON data")
    private String optimalProfileData;

    @Schema(description = "Current status", example = "ACTIVE")
    private String status;

    @Schema(description = "Owning Section ID", example = "55")
    private Long sectionId;

    // Audit fields
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Last updated by")
    private String updatedBy;
}