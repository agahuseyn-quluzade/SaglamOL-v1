package az.saglamol.userprofile.service;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.userprofile.dto.request.UpsertAgentProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertDoctorProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.dto.response.DoctorProfileResponse;
import az.saglamol.userprofile.dto.response.PatientProfileResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.DoctorProfile;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final InsuranceCompanyRepository insuranceCompanyRepository;
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository;
    private final RoleChecker roleChecker;
    private final ProviderAccessService providerAccessService;
    private final ProfileMapper profileMapper;

    public UserProfileService(
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            AgentProfileRepository agentProfileRepository,
            InsuranceCompanyRepository insuranceCompanyRepository,
            DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository,
            RoleChecker roleChecker,
            ProviderAccessService providerAccessService,
            ProfileMapper profileMapper
    ) {
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.insuranceCompanyRepository = insuranceCompanyRepository;
        this.doctorHospitalAssignmentRepository = doctorHospitalAssignmentRepository;
        this.roleChecker = roleChecker;
        this.providerAccessService = providerAccessService;
        this.profileMapper = profileMapper;
    }

    @Transactional
    public PatientProfileResponse createPatientProfile(UpsertPatientProfileRequest request) {
        roleChecker.requireAnyRole(RoleConstants.PATIENT, RoleConstants.ADMIN);
        UUID iamUserId = currentUserId();
        if (patientProfileRepository.existsByIamUserId(iamUserId)) {
            throw conflict("Patient profile already exists");
        }
        Instant now = Instant.now();
        PatientProfile profile = new PatientProfile(
                UUID.randomUUID(),
                iamUserId,
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender(),
                request.phone(),
                request.email(),
                request.nationalId(),
                profileMapper.toAddress(request.address()),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                requestedStatus(request.profileStatus()),
                now,
                now
        );
        return profileMapper.toResponse(patientProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse myPatientProfile() {
        roleChecker.requireAnyRole(RoleConstants.PATIENT, RoleConstants.ADMIN);
        return profileMapper.toResponse(patientByIamUserId(currentUserId()));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse patient(UUID profileId) {
        PatientProfile profile = patientById(profileId);
        requirePatientAccess(profile);
        return profileMapper.toResponse(profile);
    }

    @Transactional
    public PatientProfileResponse updatePatientProfile(UUID profileId, UpsertPatientProfileRequest request) {
        PatientProfile profile = patientById(profileId);
        requirePatientManage(profile);
        ProfileStatus nextStatus = requestedStatus(request.profileStatus());
        validateStatusChange(profile.getProfileStatus(), nextStatus);
        profile.update(
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender(),
                request.phone(),
                request.email(),
                request.nationalId(),
                profileMapper.toAddress(request.address()),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                nextStatus,
                Instant.now()
        );
        return profileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<PatientProfileResponse> searchPatients(String query, ProfileStatus status, String name,
                                                       String email, Pageable pageable) {
        roleChecker.requireAnyRole(RoleConstants.ADMIN, RoleConstants.AGENT);
        return patientProfileRepository.search(query, status, name, email, pageable).map(profileMapper::toResponse);
    }

    @Transactional
    public DoctorProfileResponse createDoctorProfile(UpsertDoctorProfileRequest request) {
        if (roleChecker.isHospitalAdmin() && !roleChecker.isAdmin()) {
            throw forbidden("Hospital admin cannot create doctor profiles");
        }
        roleChecker.requireAnyRole(RoleConstants.DOCTOR, RoleConstants.ADMIN);
        UUID iamUserId = currentUserId();
        if (doctorProfileRepository.existsByIamUserId(iamUserId)) {
            throw conflict("Doctor profile already exists");
        }
        if (doctorProfileRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw conflict("Doctor license number already exists");
        }
        Instant now = Instant.now();
        DoctorProfile profile = new DoctorProfile(
                UUID.randomUUID(),
                iamUserId,
                request.firstName(),
                request.lastName(),
                request.licenseNumber(),
                null,
                request.specialty(),
                request.phone(),
                request.email(),
                profileMapper.toAddress(request.address()),
                requestedStatus(request.profileStatus()),
                now,
                now
        );
        return profileMapper.toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse myDoctorProfile() {
        roleChecker.requireAnyRole(RoleConstants.DOCTOR, RoleConstants.ADMIN);
        return profileMapper.toResponse(doctorByIamUserId(currentUserId()));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse doctor(UUID profileId) {
        DoctorProfile profile = doctorById(profileId);
        if (canReadDoctor(profile)) {
            return profileMapper.toResponse(profile);
        }
        throw forbidden("Doctor profile access is forbidden");
    }

    @Transactional
    public DoctorProfileResponse updateDoctorProfile(UUID profileId, UpsertDoctorProfileRequest request) {
        DoctorProfile profile = doctorById(profileId);
        requireDoctorManage(profile);
        if (doctorProfileRepository.existsByLicenseNumberAndIamUserIdNot(request.licenseNumber(), profile.getIamUserId())) {
            throw conflict("Doctor license number already exists");
        }
        ProfileStatus nextStatus = requestedStatus(request.profileStatus());
        validateStatusChange(profile.getProfileStatus(), nextStatus);
        profile.update(
                request.firstName(),
                request.lastName(),
                request.licenseNumber(),
                null,
                request.specialty(),
                request.phone(),
                request.email(),
                profileMapper.toAddress(request.address()),
                nextStatus,
                Instant.now()
        );
        return profileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<DoctorProfileResponse> searchDoctors(String query, ProfileStatus status, String name,
                                                     String email, String specialty, UUID hospitalId, Pageable pageable) {
        roleChecker.requireAnyRole(RoleConstants.ADMIN, RoleConstants.AGENT, RoleConstants.HOSPITAL_ADMIN, RoleConstants.HOSPITAL_STAFF);
        Page<DoctorProfile> page = doctorProfileRepository.search(query, status, name, email, specialty, hospitalId, pageable);
        if (roleChecker.isAdmin() || roleChecker.isAgent()) {
            return page.map(profileMapper::toResponse);
        }
        var filtered = page.getContent().stream()
                .filter(this::canReadDoctor)
                .map(profileMapper::toResponse)
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Transactional
    public AgentProfileResponse createAgentProfile(UpsertAgentProfileRequest request) {
        roleChecker.requireAnyRole(RoleConstants.AGENT, RoleConstants.ADMIN);
        UUID iamUserId = currentUserId();
        if (agentProfileRepository.existsByIamUserId(iamUserId)) {
            throw conflict("Agent profile already exists");
        }
        if (!insuranceCompanyRepository.existsById(request.insuranceCompanyId())) {
            throw notFound("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found");
        }
        if (agentProfileRepository.existsByInsuranceCompanyIdAndEmployeeCode(request.insuranceCompanyId(), request.employeeCode())) {
            throw conflict("Agent employee code already exists");
        }
        Instant now = Instant.now();
        AgentProfile profile = new AgentProfile(
                UUID.randomUUID(),
                iamUserId,
                request.insuranceCompanyId(),
                request.firstName(),
                request.lastName(),
                request.employeeCode(),
                request.department(),
                request.phone(),
                request.email(),
                requestedStatus(request.profileStatus()),
                now,
                now
        );
        return profileMapper.toResponse(agentProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public AgentProfileResponse myAgentProfile() {
        roleChecker.requireAnyRole(RoleConstants.AGENT, RoleConstants.ADMIN);
        return profileMapper.toResponse(agentByIamUserId(currentUserId()));
    }

    @Transactional(readOnly = true)
    public AgentProfileResponse agent(UUID profileId) {
        AgentProfile profile = agentById(profileId);
        requireAgentAccess(profile);
        return profileMapper.toResponse(profile);
    }

    @Transactional
    public AgentProfileResponse updateAgentProfile(UUID profileId, UpsertAgentProfileRequest request) {
        AgentProfile profile = agentById(profileId);
        requireAgentManage(profile);
        if (!insuranceCompanyRepository.existsById(request.insuranceCompanyId())) {
            throw notFound("INSURANCE_COMPANY_NOT_FOUND", "Insurance company was not found");
        }
        if (agentProfileRepository.existsByInsuranceCompanyIdAndEmployeeCodeAndIamUserIdNot(
                request.insuranceCompanyId(), request.employeeCode(), profile.getIamUserId())) {
            throw conflict("Agent employee code already exists");
        }
        ProfileStatus nextStatus = requestedStatus(request.profileStatus());
        validateStatusChange(profile.getProfileStatus(), nextStatus);
        profile.update(
                request.insuranceCompanyId(),
                request.firstName(),
                request.lastName(),
                request.employeeCode(),
                request.department(),
                request.phone(),
                request.email(),
                nextStatus,
                Instant.now()
        );
        return profileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public Page<AgentProfileResponse> searchAgents(String query, ProfileStatus status, String name,
                                                   String email, UUID companyId, Pageable pageable) {
        roleChecker.requireRole(RoleConstants.ADMIN);
        return agentProfileRepository.search(query, status, name, email, companyId, pageable).map(profileMapper::toResponse);
    }

    private boolean canReadDoctor(DoctorProfile profile) {
        if (roleChecker.isAdmin() || roleChecker.isAgent() || profile.getIamUserId().equals(currentUserId())) {
            return true;
        }
        if (roleChecker.isHospitalAdmin() || roleChecker.isHospitalStaff()) {
            var scope = providerAccessService.readableHospitalScope();
            if (scope.isEmpty()) {
                return true;
            }
            var assignmentHospitalIds = doctorHospitalAssignmentRepository.findAllByDoctorProfileId(profile.getId()).stream()
                    .map(assignment -> assignment.getHospitalId())
                    .toList();
            return assignmentHospitalIds.stream().anyMatch(scope.get()::contains);
        }
        return false;
    }

    private void requirePatientAccess(PatientProfile profile) {
        if (roleChecker.isAdmin() || roleChecker.isAgent() || profile.getIamUserId().equals(currentUserId())) {
            return;
        }
        throw forbidden("Patient profile access is forbidden");
    }

    private void requirePatientManage(PatientProfile profile) {
        if (roleChecker.isAdmin() || (roleChecker.isPatient() && profile.getIamUserId().equals(currentUserId()))) {
            return;
        }
        throw forbidden("Patient profile management is forbidden");
    }

    private void requireDoctorManage(DoctorProfile profile) {
        if (roleChecker.isAdmin() || (roleChecker.isDoctor() && profile.getIamUserId().equals(currentUserId()))) {
            return;
        }
        throw forbidden("Doctor profile management is forbidden");
    }

    private void requireAgentAccess(AgentProfile profile) {
        if (roleChecker.isAdmin() || profile.getIamUserId().equals(currentUserId())) {
            return;
        }
        throw forbidden("Agent profile access is forbidden");
    }

    private void requireAgentManage(AgentProfile profile) {
        if (roleChecker.isAdmin() || (roleChecker.isAgent() && profile.getIamUserId().equals(currentUserId()))) {
            return;
        }
        throw forbidden("Agent profile management is forbidden");
    }

    private void validateStatusChange(ProfileStatus currentStatus, ProfileStatus nextStatus) {
        if (roleChecker.isAdmin()) {
            return;
        }
        if (currentStatus == ProfileStatus.SUSPENDED || nextStatus == ProfileStatus.SUSPENDED) {
            throw forbidden("Only admin can set or update suspended profiles");
        }
    }

    private ProfileStatus requestedStatus(ProfileStatus status) {
        ProfileStatus nextStatus = status == null ? ProfileStatus.DRAFT : status;
        if (nextStatus == ProfileStatus.SUSPENDED && !roleChecker.isAdmin()) {
            throw forbidden("Only admin can create suspended profiles");
        }
        return nextStatus;
    }

    private PatientProfile patientById(UUID profileId) {
        return patientProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("PATIENT_PROFILE_NOT_FOUND", "Patient profile was not found"));
    }

    private PatientProfile patientByIamUserId(UUID iamUserId) {
        return patientProfileRepository.findByIamUserId(iamUserId)
                .orElseThrow(() -> notFound("PATIENT_PROFILE_NOT_FOUND", "Patient profile was not found"));
    }

    private DoctorProfile doctorById(UUID profileId) {
        return doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile was not found"));
    }

    private DoctorProfile doctorByIamUserId(UUID iamUserId) {
        return doctorProfileRepository.findByIamUserId(iamUserId)
                .orElseThrow(() -> notFound("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile was not found"));
    }

    private AgentProfile agentById(UUID profileId) {
        return agentProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
    }

    private AgentProfile agentByIamUserId(UUID iamUserId) {
        return agentProfileRepository.findByIamUserId(iamUserId)
                .orElseThrow(() -> notFound("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
    }

    private UUID currentUserId() {
        return AuthContextHolder.getRequired().userId();
    }

    private UserProfileException notFound(String code, String message) {
        return new UserProfileException(code, message);
    }

    private UserProfileException conflict(String message) {
        return new UserProfileException("PROFILE_ALREADY_EXISTS", message);
    }

    private UserProfileException forbidden(String message) {
        return new UserProfileException("FORBIDDEN", message);
    }
}
