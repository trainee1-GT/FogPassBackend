package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Division creation request DTO")
public class DivisionCreateRequest {
    
    @NotBlank(message = "Division name is required")
    @Size(min = 2, max = 100, message = "Division name must be between 2 and 100 characters")
    @Schema(description = "Division name", example = "Mumbai Division", required = true)
    private String name;
    
    @NotBlank(message = "Division code is required")
    @Size(min = 2, max = 10, message = "Division code must be between 2 and 10 characters")
    @Schema(description = "Division code", example = "MUM", required = true)
    private String code;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Division description", example = "Mumbai Division covering local and suburban routes")
    private String description;
    
    @NotNull(message = "Zone ID is required")
    @Schema(description = "Zone ID this division belongs to", example = "1", required = true)
    private Long zoneId;
}