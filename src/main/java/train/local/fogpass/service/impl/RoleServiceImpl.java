package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.audit.AuditAction;
import train.local.fogpass.audit.Auditable;
import train.local.fogpass.dto.masterdata.RoleCreateRequest;
import train.local.fogpass.dto.masterdata.RoleResponse;
import train.local.fogpass.dto.masterdata.RoleUpdateRequest;
import train.local.fogpass.entity.Role;
import train.local.fogpass.entity.enums.RoleStatus;
import train.local.fogpass.exception.DuplicateRoleException;
import train.local.fogpass.exception.RoleNotFoundException;
import train.local.fogpass.mapper.RoleMapper;
import train.local.fogpass.repository.RoleRepository;
import train.local.fogpass.security.RoleConstants;
import train.local.fogpass.service.RoleService;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityType = "Role")
    public RoleResponse createRole(RoleCreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateRoleException("Role name already exists: " + request.getName());
        }
        Role role = roleMapper.toEntity(request);
        role.setStatus(RoleStatus.ACTIVE);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::toResponse);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "Role")
    public RoleResponse updateRole(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));

        if (request.getName() != null && !request.getName().equalsIgnoreCase(role.getName())
                && roleRepository.existsByName(request.getName())) {
            throw new DuplicateRoleException("Role name already exists: " + request.getName());
        }

        roleMapper.updateEntity(role, request);
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "Role")
    public void updateStatus(Long id, RoleStatus status) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));

        String name = role.getName();
        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(name)
                || RoleConstants.ADMIN.equalsIgnoreCase(name)
                || RoleConstants.LOCO_PILOT.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("Cannot change status of core system role: " + name);
        }

        role.setStatus(status);
        roleRepository.save(role);
    }
}