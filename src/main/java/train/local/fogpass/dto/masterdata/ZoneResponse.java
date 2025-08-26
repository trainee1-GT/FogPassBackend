package train.local.fogpass.dto.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Zone response DTO")
public class ZoneResponse {
    
    @Schema(description = "Zone ID", example = "1")
    private Long id;
    
    @Schema(description = "Zone name", example = "Western Railway Zone")
    private String name;
    
    @Schema(description = "Zone code", example = "WR")
    private String code;
    
    @Schema(description = "Zone description", example = "Western Railway Zone covering Mumbai and surrounding areas")
    private String description;
    
    @Schema(description = "Number of divisions in this zone", example = "5")
    private int divisionCount;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Created by user")
    private String createdBy;
    
    @Schema(description = "Last updated by user")
    private String updatedBy;
}