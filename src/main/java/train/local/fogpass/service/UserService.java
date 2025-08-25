package train.local.fogpass.service;

import train.local.fogpass.dto.request.UserCreateRequest;
import train.local.fogpass.dto.request.UserUpdateRequest;
import train.local.fogpass.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest createRequest);
    UserResponse updateUser(Long id, UserUpdateRequest updateRequest);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    void deleteUser(Long id);
}
