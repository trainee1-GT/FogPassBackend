package train.local.fogpass.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.dto.request.UserCreateRequest;
import train.local.fogpass.dto.request.UserUpdateRequest;
import train.local.fogpass.dto.response.ApiResponse;
import train.local.fogpass.dto.response.UserResponse;
import train.local.fogpass.entity.User;
import train.local.fogpass.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole(T(train.local.fogpass.security.RoleConstants).ADMIN, T(train.local.fogpass.security.RoleConstants).SUPER_ADMIN)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create User (public per SecurityConfig permitAll if needed)
    @PostMapping("/createUser")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", created));
    }


    // Get User by ID (DTO) → GET /api/users/id/{id}
    // URI Template Variable: {id}
    @GetMapping("id/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserEntityById(@PathVariable("id") Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
    }





    // Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
    }

    // Get All Users
    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users fetched successfully", users));
    }

    // Update User
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully",
                userService.updateUser(id, request)));
    }

    // Delete User (SUPER_ADMIN only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(T(train.local.fogpass.security.RoleConstants).SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
    }
}
