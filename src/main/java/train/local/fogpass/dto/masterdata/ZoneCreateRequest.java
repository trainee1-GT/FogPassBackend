package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Zone creation request DTO")
public class ZoneCreateRequest {
    
    @NotBlank(message = "Zone name is required")
    @Size(min = 2, max = 100, message = "Zone name must be between 2 and 100 characters")
    @Schema(description = "Zone name", example = "Western Railway Zone", required = true)
    private String name;
    
    @NotBlank(message = "Zone code is required")
    @Size(min = 2, max = 10, message = "Zone code must be between 2 and 10 characters")
    @Schema(description = "Zone code", example = "WR", required = true)
    private String code;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Zone description", example = "Western Railway Zone covering Mumbai and surrounding areas")
    private String description;
}