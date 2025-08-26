package train.local.fogpass.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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

        // Audit logging - Log the update operation attempt
        logger.info("Admin user {} attempting to update user with ID: {}", 
                   adminPrincipal.getUsername(), targetUserId);

        boolean isSuperAdmin = adminPrincipal.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_" + RoleConstants.SUPER_ADMIN));
        if (!isSuperAdmin) {
            if (!isScopeAllowed(adminPrincipal, targetUser)) {
                throw new AccessDeniedException("Admin is not authorized to manage this user's scope.");
            }
        }

        if (updateRequest.getUsername() != null) targetUser.setUsername(updateRequest.getUsername());
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            targetUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        if (updateRequest.getUserId() != null) targetUser.setUserId(updateRequest.getUserId());
        if (updateRequest.getLocoPilotId() != null) targetUser.setLocoPilotId(updateRequest.getLocoPilotId());
        if (updateRequest.getDateOfBirth() != null) targetUser.setDateOfBirth(updateRequest.getDateOfBirth());
        if (updateRequest.getDesignation() != null) targetUser.setDesignation(updateRequest.getDesignation());
        if (updateRequest.getDepartment() != null) targetUser.setDepartment(updateRequest.getDepartment());
        if (updateRequest.getActive() != null) targetUser.setActive(updateRequest.getActive());
        if (updateRequest.getMobNo() != null) targetUser.setMobNo(updateRequest.getMobNo());

        if (updateRequest.getRoles() != null) {
            // Clear existing scopes to handle orphan removal properly
            targetUser.getAccessScopes().clear();
            
            // If roles are provided (not empty), create new scopes
            if (!updateRequest.getRoles().isEmpty()) {
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
                
                // Add new scopes to the existing collection
                targetUser.getAccessScopes().addAll(newScopes);
            }
            // If roles array is empty, scopes remain cleared (user has no roles)
        }

        User saved = userRepository.save(targetUser);
        
        // Audit logging - Log successful update
        logger.info("Admin user {} successfully updated user with ID: {}. Target user: {}", 
                   adminPrincipal.getUsername(), targetUserId, targetUser.getUsername());
        
        return toResponse(saved);
    }

    private boolean isScopeAllowed(UserPrincipal admin, User targetUser) {
        Set<UserPrincipal.ScopeView> adminScopes = admin.getAccessScopes();
        Set<UserAccessScope> targetUserScopes = targetUser.getAccessScopes();

        if (adminScopes == null || targetUserScopes == null || adminScopes.isEmpty() || targetUserScopes.isEmpty()) {
            return false;
        }

        return adminScopes.stream().anyMatch(adminScope ->
                targetUserScopes.stream().anyMatch(targetScope -> {
                    if (adminScope.getZoneId() != null && adminScope.getDivisionId() == null) {
                        return adminScope.getZoneId().equals(targetScope.getZoneId());
                    }
                    if (adminScope.getDivisionId() != null && adminScope.getSectionId() == null) {
                        return adminScope.getDivisionId().equals(targetScope.getDivisionId());
                    }
                    if (adminScope.getSectionId() != null) {
                        return adminScope.getSectionId().equals(targetScope.getSectionId());
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
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersForAdmin(UserPrincipal adminPrincipal) {
        // Super Admin can see all users
        if (adminPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return getAllUsers();
        }

        // Get admin's access scopes
        Set<UserPrincipal.ScopeView> adminScopes = adminPrincipal.getAccessScopes();
        if (adminScopes == null || adminScopes.isEmpty()) {
            // Admin with no scopes can see all users (global admin)
            return getAllUsers();
        }

        // Filter users based on admin's scope
        return userRepository.findAll().stream()
                .filter(user -> isUserInAdminScope(user, adminScopes))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isUserInAdminScope(User user, Set<UserPrincipal.ScopeView> adminScopes) {
        Set<UserAccessScope> userScopes = user.getAccessScopes();
        
        // If user has no scopes, only global admins (no scope restrictions) can see them
        if (userScopes == null || userScopes.isEmpty()) {
            return adminScopes.stream().anyMatch(adminScope -> 
                adminScope.getZoneId() == null && 
                adminScope.getDivisionId() == null && 
                adminScope.getSectionId() == null
            );
        }

        // Check if any of the user's scopes fall within admin's scope
        return userScopes.stream().anyMatch(userScope -> 
            adminScopes.stream().anyMatch(adminScope -> isScopeWithinAdminScope(userScope, adminScope))
        );
    }

    private boolean isScopeWithinAdminScope(UserAccessScope userScope, UserPrincipal.ScopeView adminScope) {
        // Admin with no zone restriction can see all users
        if (adminScope.getZoneId() == null) {
            return true;
        }

        // User must be in the same zone as admin
        if (!Objects.equals(userScope.getZoneId(), adminScope.getZoneId())) {
            return false;
        }

        // Admin with no division restriction can see all users in their zone
        if (adminScope.getDivisionId() == null) {
            return true;
        }

        // User must be in the same division as admin
        if (!Objects.equals(userScope.getDivisionId(), adminScope.getDivisionId())) {
            return false;
        }

        // Admin with no section restriction can see all users in their division
        if (adminScope.getSectionId() == null) {
            return true;
        }

        // User must be in the same section as admin
        return Objects.equals(userScope.getSectionId(), adminScope.getSectionId());
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
