package train.local.fogpass.dto.masterdata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionCreateRequest {
    @NotNull(message = "Division ID is required")
    private Long divisionId;
    
    @NotBlank(message = "Name is required")
    private String name;
}
