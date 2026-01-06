package ma.albaraka.bank.service;

import java.util.List;

import ma.albaraka.bank.dto.request.CreateUserRequest;
import ma.albaraka.bank.dto.request.UpdateUserRequest;
import ma.albaraka.bank.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

    UserResponse updateUserRole(Long id, String role);
}
