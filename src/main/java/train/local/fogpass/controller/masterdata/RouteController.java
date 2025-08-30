package train.local.fogpass.controller.masterdata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import train.local.fogpass.dto.masterdata.RouteCreateRequest;
import train.local.fogpass.dto.masterdata.RouteResponse;
import train.local.fogpass.dto.masterdata.RouteUpdateRequest;
import train.local.fogpass.entity.enums.RouteStatus;
import train.local.fogpass.service.RouteService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/masterdata/routes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "Route Management", description = "APIs for managing Routes")
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    @Operation(summary = "Create a new Route", description = "Creates a new route under a given Section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Route created",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = train.local.fogpass.dto.response.ApiResponse.class)))
    })
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<RouteResponse>> createRoute(
            @Valid @RequestBody RouteCreateRequest request) {
        RouteResponse created = routeService.createRoute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        train.local.fogpass.dto.response.ApiResponse<RouteResponse> body =
                new train.local.fogpass.dto.response.ApiResponse<>(true, "Route created successfully", created);
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/by-section/{sectionId}")
    @Operation(summary = "Get active routes by Section", description = "Returns only ACTIVE routes for the specified Section")
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<List<RouteResponse>>> getRoutesBySection(
            @Parameter(description = "Section ID", required = true) @PathVariable Long sectionId) {
        List<RouteResponse> routes = routeService.getRoutesBySection(sectionId);
        return ResponseEntity.ok(new train.local.fogpass.dto.response.ApiResponse<>(true, "Routes retrieved", routes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID", description = "Get a single route by its identifier")
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<RouteResponse>> getRouteById(
            @Parameter(description = "Route ID", required = true) @PathVariable Long id) {
        RouteResponse route = routeService.getRouteById(id);
        return ResponseEntity.ok(new train.local.fogpass.dto.response.ApiResponse<>(true, "Route retrieved", route));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update route (partial)", description = "Partially update route fields")
    public ResponseEntity<train.local.fogpass.dto.response.ApiResponse<RouteResponse>> updateRoute(
            @Parameter(description = "Route ID", required = true) @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request) {
        RouteResponse updated = routeService.updateRoute(id, request);
        return ResponseEntity.ok(new train.local.fogpass.dto.response.ApiResponse<>(true, "Route updated", updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update route status", description = "Activate/Deactivate a route")
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "Route ID", required = true) @PathVariable Long id,
            @RequestParam("status") RouteStatus status) {
        routeService.updateRouteStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}