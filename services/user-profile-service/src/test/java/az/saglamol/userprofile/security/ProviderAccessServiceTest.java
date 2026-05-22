package az.saglamol.userprofile.security;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.HospitalBranch;
import az.saglamol.userprofile.entity.HospitalStaffProfile;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalBranchRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderAccessServiceTest {

    private final HospitalStaffProfileRepository hospitalStaffProfileRepository = mock(HospitalStaffProfileRepository.class);
    private final HospitalBranchRepository hospitalBranchRepository = mock(HospitalBranchRepository.class);
    private final DoctorProfileRepository doctorProfileRepository = mock(DoctorProfileRepository.class);
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository = mock(DoctorHospitalAssignmentRepository.class);
    private final ProviderAccessService service = new ProviderAccessService(
            new RoleChecker(),
            hospitalStaffProfileRepository,
            hospitalBranchRepository,
            doctorProfileRepository,
            doctorHospitalAssignmentRepository
    );

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void hospitalAdminCanWriteHospitalData() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(userId, RoleConstants.HOSPITAL_ADMIN);
        mockStaff(userId, hospitalId, null);

        assertDoesNotThrow(() -> service.requireHospitalWrite(hospitalId));
    }

    @Test
    void adminCanManageAllHospitals() {
        UUID userId = UUID.randomUUID();
        setContext(userId, RoleConstants.ADMIN);

        assertTrue(service.canManageHospital(userId, UUID.randomUUID()));
        assertTrue(service.canViewHospital(userId, UUID.randomUUID()));
    }

    @Test
    void hospitalAdminCannotManageAnotherHospital() {
        UUID userId = UUID.randomUUID();
        UUID ownHospitalId = UUID.randomUUID();
        UUID anotherHospitalId = UUID.randomUUID();
        setContext(userId, RoleConstants.HOSPITAL_ADMIN);
        mockStaff(userId, ownHospitalId, null);

        assertFalse(service.canManageHospital(userId, anotherHospitalId));
    }

    @Test
    void hospitalStaffCanViewOwnHospitalButCannotManageIt() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(userId, RoleConstants.HOSPITAL_STAFF);
        mockStaff(userId, hospitalId, null);

        assertTrue(service.canViewHospital(userId, hospitalId));
        assertFalse(service.canManageHospital(userId, hospitalId));
    }

    @Test
    void hospitalStaffCanViewOwnBranchOnly() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID ownBranchId = UUID.randomUUID();
        UUID anotherBranchId = UUID.randomUUID();
        setContext(userId, RoleConstants.HOSPITAL_STAFF);
        mockStaff(userId, hospitalId, ownBranchId);
        when(hospitalBranchRepository.findById(ownBranchId)).thenReturn(Optional.of(branch(ownBranchId, hospitalId)));
        when(hospitalBranchRepository.findById(anotherBranchId)).thenReturn(Optional.of(branch(anotherBranchId, hospitalId)));

        assertTrue(service.canViewBranch(userId, ownBranchId));
        assertFalse(service.canViewBranch(userId, anotherBranchId));
    }

    @Test
    void doctorCanViewAssignedHospital() {
        UUID userId = UUID.randomUUID();
        UUID doctorProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(userId, RoleConstants.DOCTOR);
        when(doctorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(new DoctorProfile(
                doctorProfileId,
                userId,
                "LIC-1",
                null,
                "Cardiology",
                Instant.now()
        )));
        when(doctorHospitalAssignmentRepository.existsByDoctorProfileIdAndHospitalId(doctorProfileId, hospitalId))
                .thenReturn(true);

        assertTrue(service.canDoctorAccessHospital(userId, hospitalId));
        assertTrue(service.canViewHospital(userId, hospitalId));
    }

    @Test
    void doctorCannotViewUnassignedHospital() {
        UUID userId = UUID.randomUUID();
        UUID doctorProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(userId, RoleConstants.DOCTOR);
        when(doctorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(new DoctorProfile(
                doctorProfileId,
                userId,
                "LIC-1",
                null,
                "Cardiology",
                Instant.now()
        )));

        assertFalse(service.canDoctorAccessHospital(userId, hospitalId));
        assertFalse(service.canViewHospital(userId, hospitalId));
    }

    @Test
    void patientCannotWriteHospitalData() {
        setRoles(RoleConstants.PATIENT);

        assertThrows(UserProfileException.class, () -> service.requireHospitalWrite(UUID.randomUUID()));
    }

    @Test
    void hospitalAdminCannotAssignDoctorToAnotherHospital() {
        UUID userId = UUID.randomUUID();
        UUID ownHospitalId = UUID.randomUUID();
        UUID anotherHospitalId = UUID.randomUUID();
        UUID doctorProfileId = UUID.randomUUID();
        setContext(userId, RoleConstants.HOSPITAL_ADMIN);
        mockStaff(userId, ownHospitalId, null);
        when(doctorProfileRepository.existsById(doctorProfileId)).thenReturn(true);

        assertFalse(service.canAssignDoctor(userId, anotherHospitalId, doctorProfileId));
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

    private void mockStaff(UUID userId, UUID hospitalId, UUID branchId) {
        when(hospitalStaffProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new HospitalStaffProfile(
                        UUID.randomUUID(),
                        userId,
                        hospitalId,
                        branchId,
                        "Staff",
                        Instant.now()
                )));
    }

    private HospitalBranch branch(UUID branchId, UUID hospitalId) {
        return new HospitalBranch(
                branchId,
                hospitalId,
                "Main",
                "Baku",
                "Address",
                null,
                Instant.now()
        );
    }
}
