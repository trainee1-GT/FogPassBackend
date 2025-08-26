package train.local.fogpass.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {
    
    @Schema(description = "List of items in current page")
    private List<T> content;
    
    @Schema(description = "Current page number (0-based)")
    private int pageNumber;
    
    @Schema(description = "Number of items per page")
    private int pageSize;
    
    @Schema(description = "Total number of items across all pages")
    private long totalElements;
    
    @Schema(description = "Total number of pages")
    private int totalPages;
    
    @Schema(description = "Whether this is the first page")
    private boolean first;
    
    @Schema(description = "Whether this is the last page")
    private boolean last;
}