package train.local.fogpass.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import train.local.fogpass.dto.masterdata.RoleCreateRequest;
import train.local.fogpass.dto.masterdata.RoleResponse;
import train.local.fogpass.dto.masterdata.RoleUpdateRequest;
import train.local.fogpass.entity.enums.RoleStatus;

public interface RoleService {
    RoleResponse createRole(RoleCreateRequest request);
    RoleResponse getRole(Long id);
    Page<RoleResponse> getAllRoles(Pageable pageable);
    RoleResponse updateRole(Long id, RoleUpdateRequest request);
    void updateStatus(Long id, RoleStatus status);
}