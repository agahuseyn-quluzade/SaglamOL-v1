package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.InternalAuthException;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.PatientProfile;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.PatientProfileRepository;
import az.saglamol.userprofile.security.ProviderAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {

    private final PatientProfileRepository patientProfileRepository = mock(PatientProfileRepository.class);
    private final DoctorProfileRepository doctorProfileRepository = mock(DoctorProfileRepository.class);
    private final AgentProfileRepository agentProfileRepository = mock(AgentProfileRepository.class);
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository = mock(DoctorHospitalAssignmentRepository.class);
    private final ProviderAccessService providerAccessService = mock(ProviderAccessService.class);
    private final UserProfileService service = new UserProfileService(
            patientProfileRepository,
            doctorProfileRepository,
            agentProfileRepository,
            doctorHospitalAssignmentRepository,
            new RoleChecker(),
            providerAccessService
    );

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void createPatientProfileUsesAuthenticatedUserId() {
        UUID userId = UUID.randomUUID();
        setContext(userId, RoleConstants.PATIENT);
        when(patientProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createMyPatientProfile(new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                LocalDate.of(1995, 1, 1),
                "+994501234567"
        ));

        assertEquals(userId, response.userId());
        assertEquals("Aga", response.firstName());
    }

    @Test
    void nonPatientCannotCreatePatientProfile() {
        setContext(UUID.randomUUID(), RoleConstants.DOCTOR);

        assertThrows(InternalAuthException.class, () -> service.createMyPatientProfile(new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                null,
                null
        )));
    }

    @Test
    void hospitalStaffCanReadAssignedDoctorInOwnHospitalScope() {
        UUID staffUserId = UUID.randomUUID();
        UUID doctorProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        setContext(staffUserId, RoleConstants.HOSPITAL_STAFF);
        when(providerAccessService.readableHospitalScope()).thenReturn(Optional.of(Set.of(hospitalId)));
        when(doctorProfileRepository.findById(doctorProfileId)).thenReturn(Optional.of(new DoctorProfile(
                doctorProfileId,
                UUID.randomUUID(),
                "LIC-1",
                null,
                "Cardiology",
                Instant.now()
        )));
        when(doctorHospitalAssignmentRepository.findAllByDoctorProfileId(doctorProfileId)).thenReturn(List.of(
                new DoctorHospitalAssignment(UUID.randomUUID(), doctorProfileId, hospitalId, null, "Cardiology", true, Instant.now())
        ));

        var response = service.doctor(doctorProfileId);

        assertEquals(doctorProfileId, response.id());
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
