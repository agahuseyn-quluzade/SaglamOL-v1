package az.saglamol.iam.service;

import az.saglamol.common.security.RoleConstants;
import az.saglamol.iam.dto.request.AssignRoleRequest;
import az.saglamol.iam.dto.request.UpdateUserStatusRequest;
import az.saglamol.iam.entity.Role;
import az.saglamol.iam.entity.UserAccount;
import az.saglamol.iam.entity.UserStatus;
import az.saglamol.iam.repository.RoleRepository;
import az.saglamol.iam.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserManagementServiceTest {

    private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final UserManagementService service = new UserManagementService(userAccountRepository, roleRepository);

    @Test
    void assignsRoleToUser() {
        UUID userId = UUID.randomUUID();
        UserAccount user = user();
        Role admin = new Role(UUID.randomUUID(), RoleConstants.ADMIN, "Admin");
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByCode(RoleConstants.ADMIN)).thenReturn(Optional.of(admin));

        var response = service.assignRole(userId, new AssignRoleRequest("ADMIN"));

        assertTrue(response.roles().contains("ADMIN"));
    }

    @Test
    void changesUserStatus() {
        UUID userId = UUID.randomUUID();
        UserAccount user = user();
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = service.changeStatus(userId, new UpdateUserStatusRequest(UserStatus.LOCKED));

        assertEquals("LOCKED", response.status());
        assertEquals(UserStatus.LOCKED, user.getStatus());
    }

    private UserAccount user() {
        UserAccount user = new UserAccount(UUID.randomUUID(), "user@gmail.com", "+994501234567", "hash", UserStatus.ACTIVE, Instant.now());
        user.addRole(new Role(UUID.randomUUID(), RoleConstants.PATIENT, "Patient"));
        return user;
    }
}
