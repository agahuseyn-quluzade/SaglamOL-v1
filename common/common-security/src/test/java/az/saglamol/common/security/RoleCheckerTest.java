package az.saglamol.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleCheckerTest {

    private final RoleChecker roleChecker = new RoleChecker();

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void checksCurrentUserRoles() {
        setRoles(RoleConstants.PATIENT, RoleConstants.HOSPITAL_STAFF);

        assertTrue(roleChecker.isPatient());
        assertTrue(roleChecker.isHospitalStaff());
        assertFalse(roleChecker.isAdmin());
        assertTrue(roleChecker.hasAnyRole(RoleConstants.ADMIN, RoleConstants.PATIENT));
    }

    @Test
    void requiresRoleOrThrowsForbidden() {
        setRoles(RoleConstants.PATIENT);

        assertDoesNotThrow(() -> roleChecker.requireRole(RoleConstants.PATIENT));
        assertThrows(InternalAuthException.class, () -> roleChecker.requireRole(RoleConstants.ADMIN));
    }

    private void setRoles(String... roles) {
        AuthContextHolder.set(new AuthContext(
                UUID.randomUUID(),
                List.of(roles),
                UUID.randomUUID().toString(),
                Map.of()
        ));
    }
}
