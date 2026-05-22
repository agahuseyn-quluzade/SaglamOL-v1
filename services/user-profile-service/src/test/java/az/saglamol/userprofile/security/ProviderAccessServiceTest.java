package az.saglamol.userprofile.security;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.InternalAuthException;
import az.saglamol.common.security.RoleChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderAccessServiceTest {

    private final ProviderAccessService service = new ProviderAccessService(new RoleChecker());

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void hospitalAdminCanWriteHospitalData() {
        setRoles("PATIENT", "HOSPITAL_ADMIN");

        assertDoesNotThrow(service::requireHospitalWrite);
    }

    @Test
    void patientCannotWriteHospitalData() {
        setRoles("PATIENT");

        assertThrows(InternalAuthException.class, service::requireHospitalWrite);
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
