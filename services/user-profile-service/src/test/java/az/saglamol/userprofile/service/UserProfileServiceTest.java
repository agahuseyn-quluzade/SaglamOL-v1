package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.InternalAuthException;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.userprofile.dto.request.UpsertAgentProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertDoctorProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.Gender;
import az.saglamol.userprofile.entity.PatientProfile;
import az.saglamol.userprofile.entity.ProfileStatus;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.mapper.ProfileMapper;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import az.saglamol.userprofile.repository.PatientProfileRepository;
import az.saglamol.userprofile.security.ProviderAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {

    private final PatientProfileRepository patientProfileRepository = mock(PatientProfileRepository.class);
    private final DoctorProfileRepository doctorProfileRepository = mock(DoctorProfileRepository.class);
    private final AgentProfileRepository agentProfileRepository = mock(AgentProfileRepository.class);
    private final InsuranceCompanyRepository insuranceCompanyRepository = mock(InsuranceCompanyRepository.class);
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository = mock(DoctorHospitalAssignmentRepository.class);
    private final ProviderAccessService providerAccessService = mock(ProviderAccessService.class);
    private final UserProfileService service = new UserProfileService(
            patientProfileRepository,
            doctorProfileRepository,
            agentProfileRepository,
            insuranceCompanyRepository,
            doctorHospitalAssignmentRepository,
            new RoleChecker(),
            providerAccessService,
            new ProfileMapper()
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

        var response = service.createPatientProfile(new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                LocalDate.of(1995, 1, 1),
                Gender.MALE,
                "+994501234567",
                "aga@saglamol.az",
                null,
                null,
                null,
                null,
                ProfileStatus.ACTIVE
        ));

        assertEquals(userId, response.iamUserId());
        assertEquals("Aga", response.firstName());
    }

    @Test
    void duplicatePatientProfileFails() {
        UUID userId = UUID.randomUUID();
        setContext(userId, RoleConstants.PATIENT);
        when(patientProfileRepository.existsByIamUserId(userId)).thenReturn(true);

        assertThrows(UserProfileException.class, () -> service.createPatientProfile(patientRequest()));
    }

    @Test
    void patientCannotAccessAnotherPatientProfile() {
        UUID currentUserId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        setContext(currentUserId, RoleConstants.PATIENT);
        when(patientProfileRepository.findById(profileId)).thenReturn(Optional.of(patient(profileId, anotherUserId)));

        assertThrows(UserProfileException.class, () -> service.patient(profileId));
    }

    @Test
    void adminCanAccessAllPatients() {
        UUID profileId = UUID.randomUUID();
        setContext(UUID.randomUUID(), RoleConstants.ADMIN);
        when(patientProfileRepository.findById(profileId)).thenReturn(Optional.of(patient(profileId, UUID.randomUUID())));

        var response = service.patient(profileId);

        assertEquals(profileId, response.id());
    }

    @Test
    void nonPatientCannotCreatePatientProfile() {
        setContext(UUID.randomUUID(), RoleConstants.DOCTOR);

        assertThrows(InternalAuthException.class, () -> service.createPatientProfile(new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                null,
                null,
                "+994501234567",
                "aga@saglamol.az",
                null,
                null,
                null,
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
        when(doctorProfileRepository.findById(doctorProfileId)).thenReturn(Optional.of(doctor(doctorProfileId, UUID.randomUUID())));
        when(doctorHospitalAssignmentRepository.findAllByDoctorProfileId(doctorProfileId)).thenReturn(List.of(
                new DoctorHospitalAssignment(UUID.randomUUID(), doctorProfileId, hospitalId, null, "Cardiology", true, Instant.now())
        ));

        var response = service.doctor(doctorProfileId);

        assertEquals(doctorProfileId, response.id());
    }

    @Test
    void doctorCreateSuccess() {
        UUID userId = UUID.randomUUID();
        setContext(userId, RoleConstants.DOCTOR);
        when(doctorProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createDoctorProfile(doctorRequest("LIC-1"));

        assertEquals(userId, response.iamUserId());
        assertEquals("LIC-1", response.licenseNumber());
    }

    @Test
    void duplicateLicenseFails() {
        setContext(UUID.randomUUID(), RoleConstants.DOCTOR);
        when(doctorProfileRepository.existsByLicenseNumber("LIC-1")).thenReturn(true);

        assertThrows(UserProfileException.class, () -> service.createDoctorProfile(doctorRequest("LIC-1")));
    }

    @Test
    void doctorCannotAccessAnotherDoctorProfile() {
        UUID profileId = UUID.randomUUID();
        setContext(UUID.randomUUID(), RoleConstants.DOCTOR);
        when(doctorProfileRepository.findById(profileId)).thenReturn(Optional.of(doctor(profileId, UUID.randomUUID())));

        assertThrows(UserProfileException.class, () -> service.doctor(profileId));
    }

    @Test
    void hospitalAdminCannotCreateDoctorProfile() {
        setContext(UUID.randomUUID(), RoleConstants.HOSPITAL_ADMIN);

        assertThrows(UserProfileException.class, () -> service.createDoctorProfile(doctorRequest("LIC-1")));
    }

    @Test
    void agentCreateSuccess() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        setContext(userId, RoleConstants.AGENT);
        when(insuranceCompanyRepository.existsById(companyId)).thenReturn(true);
        when(agentProfileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createAgentProfile(agentRequest(companyId, "AG-1"));

        assertEquals(userId, response.iamUserId());
        assertEquals("AG-1", response.employeeCode());
    }

    @Test
    void duplicateEmployeeCodeFails() {
        UUID companyId = UUID.randomUUID();
        setContext(UUID.randomUUID(), RoleConstants.AGENT);
        when(insuranceCompanyRepository.existsById(companyId)).thenReturn(true);
        when(agentProfileRepository.existsByInsuranceCompanyIdAndEmployeeCode(companyId, "AG-1")).thenReturn(true);

        assertThrows(UserProfileException.class, () -> service.createAgentProfile(agentRequest(companyId, "AG-1")));
    }

    @Test
    void agentCannotAccessAnotherAgentProfile() {
        UUID profileId = UUID.randomUUID();
        setContext(UUID.randomUUID(), RoleConstants.AGENT);
        when(agentProfileRepository.findById(profileId)).thenReturn(Optional.of(agent(profileId, UUID.randomUUID())));

        assertThrows(UserProfileException.class, () -> service.agent(profileId));
    }

    @Test
    void searchPatientsReturnsPaginatedResult() {
        setContext(UUID.randomUUID(), RoleConstants.ADMIN);
        var pageable = PageRequest.of(0, 10);
        when(patientProfileRepository.search(eq("aga"), eq(ProfileStatus.ACTIVE), eq("Aga"), eq("aga@saglamol.az"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(patient(UUID.randomUUID(), UUID.randomUUID())), pageable, 1));

        var page = service.searchPatients("aga", ProfileStatus.ACTIVE, "Aga", "aga@saglamol.az", pageable);

        assertEquals(1, page.getTotalElements());
    }

    private void setContext(UUID userId, String... roles) {
        AuthContextHolder.set(new AuthContext(
                userId,
                List.of(roles),
                UUID.randomUUID().toString(),
                Map.of()
        ));
    }

    private UpsertPatientProfileRequest patientRequest() {
        return new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                LocalDate.of(1995, 1, 1),
                Gender.MALE,
                "+994501234567",
                "aga@saglamol.az",
                null,
                null,
                null,
                null,
                ProfileStatus.ACTIVE
        );
    }

    private UpsertDoctorProfileRequest doctorRequest(String licenseNumber) {
        return new UpsertDoctorProfileRequest(
                "Doctor",
                "One",
                "Cardiology",
                licenseNumber,
                "+994501234567",
                "doctor@saglamol.az",
                null,
                ProfileStatus.ACTIVE
        );
    }

    private UpsertAgentProfileRequest agentRequest(UUID companyId, String employeeCode) {
        return new UpsertAgentProfileRequest(
                companyId,
                "Agent",
                "One",
                employeeCode,
                "Sales",
                "+994501234567",
                "agent@saglamol.az",
                ProfileStatus.ACTIVE
        );
    }

    private PatientProfile patient(UUID profileId, UUID userId) {
        Instant now = Instant.now();
        return new PatientProfile(
                profileId,
                userId,
                "Aga",
                "Huseyn",
                LocalDate.of(1995, 1, 1),
                Gender.MALE,
                "+994501234567",
                "aga@saglamol.az",
                null,
                null,
                null,
                null,
                ProfileStatus.ACTIVE,
                now,
                now
        );
    }

    private AgentProfile agent(UUID profileId, UUID userId) {
        Instant now = Instant.now();
        return new AgentProfile(
                profileId,
                userId,
                UUID.randomUUID(),
                "Agent",
                "One",
                "AG-1",
                "Sales",
                "+994501234567",
                "agent@saglamol.az",
                ProfileStatus.ACTIVE,
                now,
                now
        );
    }

    private DoctorProfile doctor(UUID doctorProfileId, UUID userId) {
        Instant now = Instant.now();
        return new DoctorProfile(
                doctorProfileId,
                userId,
                "Doctor",
                "One",
                "LIC-1",
                null,
                "Cardiology",
                "+994501234567",
                "doctor@saglamol.az",
                null,
                ProfileStatus.ACTIVE,
                now,
                now
        );
    }
}
