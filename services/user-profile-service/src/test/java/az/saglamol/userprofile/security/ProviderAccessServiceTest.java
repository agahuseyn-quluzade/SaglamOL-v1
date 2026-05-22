package az.saglamol.userprofile.security;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.userprofile.entity.HospitalStaffProfile;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderAccessServiceTest {

    private final HospitalStaffProfileRepository hospitalStaffProfileRepository = mock(HospitalStaffProfileRepository.class);
    private final ProviderAccessService service = new ProviderAccessService(
            new RoleChecker(),
            hospitalStaffProfileRepository,
            mock(DoctorProfileRepository.class),
            mock(DoctorHospitalAssignmentRepository.class)
    );

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void hospitalAdminCanWriteHospitalData() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(userId, "PATIENT", "HOSPITAL_ADMIN");
        when(hospitalStaffProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new HospitalStaffProfile(
                        UUID.randomUUID(),
                        userId,
                        hospitalId,
                        null,
                        "Admin",
                        Instant.now()
                )));

        assertDoesNotThrow(() -> service.requireHospitalWrite(hospitalId));
    }

    @Test
    void patientCannotWriteHospitalData() {
        setRoles("PATIENT");

        assertThrows(UserProfileException.class, () -> service.requireHospitalWrite(UUID.randomUUID()));
    }

    private void setRoles(String... roles) {
        setContext(UUID.randomUUID(), roles);
    }

    private void setContext(UUID userId, String... roles) {
        AuthContextHolder.set(new AuthContext(
                userId,
                List.of(roles),
                UUID.randomUUID().toString(),
                Map.of()
        ));
    }
}
