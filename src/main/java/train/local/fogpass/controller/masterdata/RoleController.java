package train.local.fogpass.controller.masterdata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import train.local.fogpass.dto.masterdata.RoleCreateRequest;
import train.local.fogpass.dto.masterdata.RoleResponse;
import train.local.fogpass.dto.masterdata.RoleUpdateRequest;
import train.local.fogpass.entity.enums.RoleStatus;
import train.local.fogpass.service.RoleService;

@RestController
@RequestMapping("/api/masterdata/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        RoleResponse created = roleService.createRole(request);
        return ResponseEntity.created(
                uriBuilder.path("/api/masterdata/roles/{id}").buildAndExpand(created.getId()).toUri()
        ).body(created);
    }

    @GetMapping("/{id}")
    public RoleResponse get(@PathVariable Long id) {
        return roleService.getRole(id);
    }

    @GetMapping
    public Page<RoleResponse> list(Pageable pageable) {
        return roleService.getAllRoles(pageable);
    }

    @PatchMapping("/{id}")
    public RoleResponse patch(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return roleService.updateRole(id, request);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateRequest request) {
        roleService.updateStatus(id, request.status);
        return ResponseEntity.noContent().build();
    }

    public static final class StatusUpdateRequest {
        @NotNull
        public RoleStatus status;
    }
}