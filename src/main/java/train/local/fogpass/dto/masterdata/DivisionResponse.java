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
@Schema(description = "Division response DTO")
public class DivisionResponse {
    
    @Schema(description = "Division ID", example = "1")
    private Long id;
    
    @Schema(description = "Division name", example = "Mumbai Division")
    private String name;
    
    @Schema(description = "Division code", example = "MUM")
    private String code;
    
    @Schema(description = "Division description", example = "Mumbai Division covering local and suburban routes")
    private String description;
    
    @Schema(description = "Zone ID this division belongs to", example = "1")
    private Long zoneId;
    
    @Schema(description = "Zone name this division belongs to", example = "Western Railway Zone")
    private String zoneName;
    
    @Schema(description = "Number of sections in this division", example = "3")
    private int sectionCount;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Created by user")
    private String createdBy;
    
    @Schema(description = "Last updated by user")
    private String updatedBy;
}