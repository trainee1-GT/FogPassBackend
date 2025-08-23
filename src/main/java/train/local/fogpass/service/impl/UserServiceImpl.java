package train.local.fogpass.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.dto.request.UserCreateRequest;
import train.local.fogpass.dto.request.UserUpdateRequest;
import train.local.fogpass.dto.response.RoleScopeDto;
import train.local.fogpass.dto.response.UserResponse;
import train.local.fogpass.entity.Role;
import train.local.fogpass.entity.User;
import train.local.fogpass.entity.UserAccessScope;
import train.local.fogpass.exception.BadRequestException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.repository.RoleRepository;
import train.local.fogpass.repository.UserRepository;
import train.local.fogpass.security.RoleConstants;
import train.local.fogpass.security.SecurityUtil;
import train.local.fogpass.security.UserPrincipal;
import train.local.fogpass.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (req.getUserId() != null && userRepository.existsByUserId(req.getUserId())) {
            throw new BadRequestException("UserId already exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setUserId(req.getUserId());
        user.setLocoPilotId(req.getLocoPilotId());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setDesignation(req.getDesignation());
        user.setDepartment(req.getDepartment());
        user.setActive(req.isActive());
        user.setMobNo(req.getMobNo());

        Set<UserAccessScope> scopes = req.getRoles() == null ? Set.of() : req.getRoles().stream().map(r -> {
            Role role = roleRepository.findByName(r.getRoleName())
                    .orElseThrow(() -> new BadRequestException("Role not found: " + r.getRoleName()));
            UserAccessScope s = new UserAccessScope();
            s.setUser(user);
            s.setRole(role);
            s.setZoneId(r.getZoneId());
            s.setDivisionId(r.getDivisionId());
            s.setSectionId(r.getSectionId());
            return s;
        }).collect(Collectors.toSet());

        user.setAccessScopes(scopes);
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long targetUserId, UserUpdateRequest updateRequest) {
        UserPrincipal adminPrincipal = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + targetUserId));

        boolean isSuperAdmin = adminPrincipal.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + RoleConstants.SUPER_ADMIN));
        if (!isSuperAdmin) {
            if (!isScopeAllowed(adminPrincipal, targetUser)) {
                throw new AccessDeniedException("Admin is not authorized to manage this user's scope.");
            }
        }

        if (updateRequest.getUsername() != null) targetUser.setUsername(updateRequest.getUsername());
        if (updateRequest.getPassword() != null) targetUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        if (updateRequest.getUserId() != null) targetUser.setUserId(updateRequest.getUserId());
        if (updateRequest.getLocoPilotId() != null) targetUser.setLocoPilotId(updateRequest.getLocoPilotId());
        if (updateRequest.getDateOfBirth() != null) targetUser.setDateOfBirth(updateRequest.getDateOfBirth());
        if (updateRequest.getDesignation() != null) targetUser.setDesignation(updateRequest.getDesignation());
        if (updateRequest.getDepartment() != null) targetUser.setDepartment(updateRequest.getDepartment());
        if (updateRequest.getActive() != null) targetUser.setActive(updateRequest.getActive());
        if (updateRequest.getMobNo() != null) targetUser.setMobNo(updateRequest.getMobNo());

        if (updateRequest.getRoles() != null && !updateRequest.getRoles().isEmpty()) {
            Set<UserAccessScope> newScopes = updateRequest.getRoles().stream().map(r -> {
                Role role = roleRepository.findByName(r.getRoleName())
                        .orElseThrow(() -> new BadRequestException("Role not found: " + r.getRoleName()));
                UserAccessScope s = new UserAccessScope();
                s.setUser(targetUser);
                s.setRole(role);
                s.setZoneId(r.getZoneId());
                s.setDivisionId(r.getDivisionId());
                s.setSectionId(r.getSectionId());
                return s;
            }).collect(Collectors.toSet());
            targetUser.setAccessScopes(newScopes);
        }

        User saved = userRepository.save(targetUser);
        return toResponse(saved);
    }

    private boolean isScopeAllowed(UserPrincipal admin, User targetUser) {
        Set<UserPrincipal.ScopeView> adminScopes = admin.getAccessScopes();
        Set<UserAccessScope> targetUserScopes = targetUser.getAccessScopes();

        if (adminScopes == null || targetUserScopes == null) return false;

        return adminScopes.stream().anyMatch(adminScope ->
                targetUserScopes.stream().anyMatch(targetScope -> {
                    if (adminScope.getZoneId() != null && adminScope.getDivisionId() == null) {
                        return Objects.equals(adminScope.getZoneId(), targetScope.getZoneId());
                    }
                    if (adminScope.getDivisionId() != null && adminScope.getSectionId() == null) {
                        return Objects.equals(adminScope.getDivisionId(), targetScope.getDivisionId());
                    }
                    if (adminScope.getSectionId() != null) {
                        return Objects.equals(adminScope.getSectionId(), targetScope.getSectionId());
                    }
                    return false;
                })
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setUserId(user.getUserId());
        dto.setLocoPilotId(user.getLocoPilotId());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setDesignation(user.getDesignation());
        dto.setDepartment(user.getDepartment());
        dto.setActive(user.isActive());
        dto.setMobNo(user.getMobNo());

        Set<RoleScopeDto> scopes = user.getAccessScopes() == null ? Set.of() : user.getAccessScopes().stream()
                .map(s -> new RoleScopeDto(
                        s.getRole() != null ? s.getRole().getName() : null,
                        s.getZoneId(),
                        s.getDivisionId(),
                        s.getSectionId()
                )).collect(Collectors.toSet());
        dto.setScopes(scopes);
        return dto;
    }
}
