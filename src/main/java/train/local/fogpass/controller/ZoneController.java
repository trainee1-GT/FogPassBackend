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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import train.local.fogpass.dto.masterdata.ZoneCreateRequest;
import train.local.fogpass.dto.masterdata.ZoneResponse;
import train.local.fogpass.dto.masterdata.ZoneUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.service.ZoneService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/masterdata/zones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Zone Management", description = "APIs for managing railway zones")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping
    @Operation(summary = "Create a new zone", description = "Creates a new railway zone with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zone created successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<ZoneResponse>> createZone(
            @Valid @RequestBody ZoneCreateRequest request) {
        
        ZoneResponse createdZone = zoneService.createZone(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdZone.getId())
                .toUri();
        
        train.local.fogpass.dto.response.ApiResponse<ZoneResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Zone created successfully", createdZone);
        
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID", description = "Retrieves a specific zone by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Zone not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<ZoneResponse>> getZoneById(
            @Parameter(description = "Zone ID", required = true) @PathVariable Long id) {
        
        ZoneResponse zone = zoneService.getZoneById(id);
        train.local.fogpass.dto.response.ApiResponse<ZoneResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Zone retrieved successfully", zone);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all zones", description = "Retrieves all zones with optional pagination and search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zones retrieved successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<PageResponse<ZoneResponse>>> getAllZones(
            @Parameter(description = "Search by zone name") @RequestParam(required = false) String name,
            @Parameter(description = "Search by zone code") @RequestParam(required = false) String code,
            @Parameter(description = "Return all results without pagination") @RequestParam(defaultValue = "false") boolean all,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        
        if (all) {
            List<ZoneResponse> zones = zoneService.getAllZones();
            PageResponse<ZoneResponse> pageResponse = new PageResponse<>();
            pageResponse.setContent(zones);
            pageResponse.setPageNumber(0);
            pageResponse.setPageSize(zones.size());
            pageResponse.setTotalElements(zones.size());
            pageResponse.setTotalPages(1);
            pageResponse.setFirst(true);
            pageResponse.setLast(true);
            
            train.local.fogpass.dto.response.ApiResponse<PageResponse<ZoneResponse>> response = 
                    new train.local.fogpass.dto.response.ApiResponse<>(true, "All zones retrieved successfully", pageResponse);
            return ResponseEntity.ok(response);
        }
        
        PageResponse<ZoneResponse> zones;
        if (name != null || code != null) {
            zones = zoneService.searchZones(name, code, pageable);
        } else {
            zones = zoneService.getAllZones(pageable);
        }
        
        train.local.fogpass.dto.response.ApiResponse<PageResponse<ZoneResponse>> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Zones retrieved successfully", zones);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update zone", description = "Updates a zone with partial data (PATCH semantics)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone updated successfully",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Zone not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<ZoneResponse>> updateZone(
            @Parameter(description = "Zone ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ZoneUpdateRequest request) {
        
        ZoneResponse updatedZone = zoneService.updateZone(id, request);
        train.local.fogpass.dto.response.ApiResponse<ZoneResponse> response = 
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Zone updated successfully", updatedZone);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete zone", description = "Deletes a zone by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zone deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete zone with existing divisions",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Zone not found",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<Void> deleteZone(
            @Parameter(description = "Zone ID", required = true) @PathVariable Long id) {
        
        zoneService.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}