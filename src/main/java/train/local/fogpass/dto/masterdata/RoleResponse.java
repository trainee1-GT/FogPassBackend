package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import train.local.fogpass.entity.enums.RoleStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RoleResponse", description = "Role master data response DTO")
public class RoleResponse {

    @Schema(description = "Database ID", example = "1")
    private Long id;

    @Schema(description = "Unique role name", example = "ADMIN")
    private String name;

    @Schema(description = "Optional description", example = "Administrators")
    private String description;

    @Schema(description = "Role status", example = "ACTIVE")
    private RoleStatus status;

    // Audit fields from BaseEntity
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Created by username")
    private String createdBy;

    @Schema(description = "Updated by username")
    private String updatedBy;
}