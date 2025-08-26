package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Division update request DTO - PATCH semantics (only non-null fields will be updated)")
public class DivisionUpdateRequest {
    
    @Size(min = 2, max = 100, message = "Division name must be between 2 and 100 characters")
    @Schema(description = "Division name", example = "Mumbai Division")
    private String name;
    
    @Size(min = 2, max = 10, message = "Division code must be between 2 and 10 characters")
    @Schema(description = "Division code", example = "MUM")
    private String code;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Division description", example = "Mumbai Division covering local and suburban routes")
    private String description;
    
    @Schema(description = "Zone ID this division belongs to", example = "1")
    private Long zoneId;
}