package train.local.fogpass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import train.local.fogpass.dto.masterdata.DivisionCreateRequest;
import train.local.fogpass.dto.masterdata.DivisionResponse;
import train.local.fogpass.dto.masterdata.DivisionUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.service.DivisionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/master-data/divisions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ZONE_ADMIN')")
@Tag(name = "Division Management", description = "APIs for managing railway divisions")
public class DivisionController {

    private final DivisionService divisionService;

    @PostMapping
    @Operation(summary = "Create a new division", description = "Creates a new railway division within a zone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Division created successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or zone not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<DivisionResponse>> createDivision(
            @Valid @RequestBody DivisionCreateRequest request) {
        
        DivisionResponse createdDivision = divisionService.createDivision(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDivision.getId())
                .toUri();
        
        train.local.fogpass.dto.response.ApiResponse<DivisionResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Division created successfully", createdDivision);
        
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get division by ID", description = "Retrieves a specific division by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Division found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Division not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<DivisionResponse>> getDivisionById(
            @Parameter(description = "Division ID", required = true) @PathVariable Long id) {
        
        DivisionResponse division = divisionService.getDivisionById(id);
        train.local.fogpass.dto.response.ApiResponse<DivisionResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Division retrieved successfully", division);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all divisions", description = "Retrieves divisions with optional filtering and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Divisions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<PageResponse<DivisionResponse>>> getAllDivisions(
            @Parameter(description = "Filter by zone ID") @RequestParam(required = false) Long zoneId,
            @Parameter(description = "Search by division name") @RequestParam(required = false) String name,
            @Parameter(description = "Search by division code") @RequestParam(required = false) String code,
            @Parameter(description = "Return all results without pagination") @RequestParam(defaultValue = "false") boolean all,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        
        if (all && zoneId != null) {
            List<DivisionResponse> divisions = divisionService.getDivisionsByZone(zoneId);
            PageResponse<DivisionResponse> pageResponse = new PageResponse<>();
            pageResponse.setContent(divisions);
            pageResponse.setPageNumber(0);
            pageResponse.setPageSize(divisions.size());
            pageResponse.setTotalElements(divisions.size());
            pageResponse.setTotalPages(1);
            pageResponse.setFirst(true);
            pageResponse.setLast(true);
            
            train.local.fogpass.dto.response.ApiResponse<PageResponse<DivisionResponse>> response = 
                    new train.local.fogpass.dto.response.ApiResponse<>(true, "All divisions retrieved successfully", pageResponse);
            return ResponseEntity.ok(response);
        }
        
        if (all) {
            List<DivisionResponse> divisions = divisionService.getAllDivisions();
            PageResponse<DivisionResponse> pageResponse = new PageResponse<>();
            pageResponse.setContent(divisions);
            pageResponse.setPageNumber(0);
            pageResponse.setPageSize(divisions.size());
            pageResponse.setTotalElements(divisions.size());
            pageResponse.setTotalPages(1);
            pageResponse.setFirst(true);
            pageResponse.setLast(true);
            
            train.local.fogpass.dto.response.ApiResponse<PageResponse<DivisionResponse>> response = 
                    new train.local.fogpass.dto.response.ApiResponse<>(true, "All divisions retrieved successfully", pageResponse);
            return ResponseEntity.ok(response);
        }
        
        PageResponse<DivisionResponse> divisions;
        if (zoneId != null && (name != null || code != null)) {
            divisions = divisionService.searchDivisions(zoneId, name, code, pageable);
        } else if (zoneId != null) {
            divisions = divisionService.getDivisionsByZone(zoneId, pageable);
        } else if (name != null || code != null) {
            divisions = divisionService.searchDivisions(null, name, code, pageable);
        } else {
            divisions = divisionService.getAllDivisions(pageable);
        }
        
        train.local.fogpass.dto.response.ApiResponse<PageResponse<DivisionResponse>> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Divisions retrieved successfully", divisions);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update division", description = "Updates a division with partial data (PATCH semantics)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Division updated successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Division not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<DivisionResponse>> updateDivision(
            @Parameter(description = "Division ID", required = true) @PathVariable Long id,
            @Valid @RequestBody DivisionUpdateRequest request) {
        
        DivisionResponse updatedDivision = divisionService.updateDivision(id, request);
        train.local.fogpass.dto.response.ApiResponse<DivisionResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Division updated successfully", updatedDivision);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete division", description = "Deletes a division by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Division deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete division with existing sections",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Division not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<Void> deleteDivision(
            @Parameter(description = "Division ID", required = true) @PathVariable Long id) {
        
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }
}