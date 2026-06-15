package az.saglamol.iam.service;

import az.saglamol.iam.dto.request.AssignRoleRequest;
import az.saglamol.iam.dto.request.UpdateUserStatusRequest;
import az.saglamol.iam.dto.response.UserResponse;
import az.saglamol.iam.entity.Permission;
import az.saglamol.iam.entity.Role;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.exception.IamException;
import az.saglamol.iam.repository.RoleRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserManagementService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;

    public UserManagementService(UserAccountRepository userAccountRepository, RoleRepository roleRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(UserAccount::getEmail))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse user(UUID id) {
        return toResponse(findUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.length() < 2) {
            throw new IamException("INVALID_SEARCH_QUERY", "Search query must contain at least 2 characters");
        }
        return userAccountRepository.search(normalized).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse changeStatus(UUID id, UpdateUserStatusRequest request) {
        UserAccount user = findUser(id);
        user.changeStatus(request.status());
        return toResponse(user);
    }

    @Transactional
    public UserResponse assignRole(UUID id, AssignRoleRequest request) {
        UserAccount user = findUser(id);
        Role role = findRole(request.roleName());
        user.addRole(role);
        return toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(UUID id, String roleName) {
        UserAccount user = findUser(id);
        Role role = findRole(roleName);
        user.removeRole(role);
        return toResponse(user);
    }

    private UserAccount findUser(UUID id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IamException("USER_NOT_FOUND", "User was not found"));
    }

    private Role findRole(String roleName) {
        return roleRepository.findByCode(roleName.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new IamException("ROLE_NOT_FOUND", "Role was not found"));
    }

    private UserResponse toResponse(UserAccount user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                roles,
                permissions
        );
    }
}
