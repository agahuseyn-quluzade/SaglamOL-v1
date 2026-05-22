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
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.PatientProfileRepository;
import az.saglamol.userprofile.security.ProviderAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AgentProfileRepository agentProfileRepository;
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository;
    private final RoleChecker roleChecker;
    private final ProviderAccessService providerAccessService;

    public UserProfileService(
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            AgentProfileRepository agentProfileRepository,
            DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository,
            RoleChecker roleChecker,
            ProviderAccessService providerAccessService
    ) {
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.agentProfileRepository = agentProfileRepository;
        this.doctorHospitalAssignmentRepository = doctorHospitalAssignmentRepository;
        this.roleChecker = roleChecker;
        this.providerAccessService = providerAccessService;
    }

    @Transactional
    public PatientProfileResponse createMyPatientProfile(UpsertPatientProfileRequest request) {
        roleChecker.requireRole(RoleConstants.PATIENT);
        UUID userId = currentUserId();
        if (patientProfileRepository.existsByUserId(userId)) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Patient profile already exists");
        }
        PatientProfile profile = new PatientProfile(
                UUID.randomUUID(),
                userId,
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.phone(),
                Instant.now()
        );
        return toResponse(patientProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse myPatientProfile() {
        roleChecker.requireRole(RoleConstants.PATIENT);
        return toResponse(patientByUserId(currentUserId()));
    }

    @Transactional
    public PatientProfileResponse updateMyPatientProfile(UpsertPatientProfileRequest request) {
        roleChecker.requireRole(RoleConstants.PATIENT);
        PatientProfile profile = patientByUserId(currentUserId());
        profile.update(request.firstName(), request.lastName(), request.dateOfBirth(), request.phone());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<PatientProfileResponse> patients() {
        roleChecker.requireAnyRole(RoleConstants.ADMIN, RoleConstants.AGENT);
        return patientProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse patient(UUID profileId) {
        PatientProfile profile = patientProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("PATIENT_PROFILE_NOT_FOUND", "Patient profile was not found"));
        if (roleChecker.isAdmin() || roleChecker.isAgent() || profile.getUserId().equals(currentUserId())) {
            return toResponse(profile);
        }
        throw forbidden("Patient profile access is forbidden");
    }

    @Transactional
    public DoctorProfileResponse createMyDoctorProfile(UpsertDoctorProfileRequest request) {
        roleChecker.requireRole(RoleConstants.DOCTOR);
        UUID userId = currentUserId();
        if (doctorProfileRepository.existsByUserId(userId)) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Doctor profile already exists");
        }
        if (doctorProfileRepository.existsByLicenseNo(request.licenseNo())) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Doctor license number already exists");
        }
        DoctorProfile profile = new DoctorProfile(
                UUID.randomUUID(),
                userId,
                request.licenseNo(),
                request.hospitalId(),
                request.specialty(),
                Instant.now()
        );
        return toResponse(doctorProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse myDoctorProfile() {
        roleChecker.requireRole(RoleConstants.DOCTOR);
        return toResponse(doctorByUserId(currentUserId()));
    }

    @Transactional
    public DoctorProfileResponse updateMyDoctorProfile(UpsertDoctorProfileRequest request) {
        roleChecker.requireRole(RoleConstants.DOCTOR);
        UUID userId = currentUserId();
        DoctorProfile profile = doctorByUserId(userId);
        if (doctorProfileRepository.existsByLicenseNoAndUserIdNot(request.licenseNo(), userId)) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Doctor license number already exists");
        }
        profile.update(request.licenseNo(), request.hospitalId(), request.specialty());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> doctors() {
        roleChecker.requireAnyRole(RoleConstants.ADMIN, RoleConstants.AGENT, RoleConstants.HOSPITAL_ADMIN, RoleConstants.HOSPITAL_STAFF);
        return doctorProfileRepository.findAll().stream()
                .filter(this::canReadDoctor)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse doctor(UUID profileId) {
        DoctorProfile profile = doctorProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile was not found"));
        if (canReadDoctor(profile)) {
            return toResponse(profile);
        }
        throw forbidden("Doctor profile access is forbidden");
    }

    @Transactional
    public AgentProfileResponse createMyAgentProfile(UpsertAgentProfileRequest request) {
        roleChecker.requireRole(RoleConstants.AGENT);
        UUID userId = currentUserId();
        if (agentProfileRepository.existsByUserId(userId)) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Agent profile already exists");
        }
        if (agentProfileRepository.existsByEmployeeNo(request.employeeNo())) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Agent employee number already exists");
        }
        AgentProfile profile = new AgentProfile(
                UUID.randomUUID(),
                userId,
                request.employeeNo(),
                request.department(),
                Instant.now()
        );
        return toResponse(agentProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public AgentProfileResponse myAgentProfile() {
        roleChecker.requireRole(RoleConstants.AGENT);
        return toResponse(agentByUserId(currentUserId()));
    }

    @Transactional
    public AgentProfileResponse updateMyAgentProfile(UpsertAgentProfileRequest request) {
        roleChecker.requireRole(RoleConstants.AGENT);
        UUID userId = currentUserId();
        AgentProfile profile = agentByUserId(userId);
        if (agentProfileRepository.existsByEmployeeNoAndUserIdNot(request.employeeNo(), userId)) {
            throw new UserProfileException("PROFILE_ALREADY_EXISTS", "Agent employee number already exists");
        }
        profile.update(request.employeeNo(), request.department());
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<AgentProfileResponse> agents() {
        roleChecker.requireRole(RoleConstants.ADMIN);
        return agentProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgentProfileResponse agent(UUID profileId) {
        AgentProfile profile = agentProfileRepository.findById(profileId)
                .orElseThrow(() -> notFound("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
        if (roleChecker.isAdmin() || profile.getUserId().equals(currentUserId())) {
            return toResponse(profile);
        }
        throw forbidden("Agent profile access is forbidden");
    }

    private boolean canReadDoctor(DoctorProfile profile) {
        if (roleChecker.isAdmin() || roleChecker.isAgent() || profile.getUserId().equals(currentUserId())) {
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

    private PatientProfile patientByUserId(UUID userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("PATIENT_PROFILE_NOT_FOUND", "Patient profile was not found"));
    }

    private DoctorProfile doctorByUserId(UUID userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile was not found"));
    }

    private AgentProfile agentByUserId(UUID userId) {
        return agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("AGENT_PROFILE_NOT_FOUND", "Agent profile was not found"));
    }

    private UUID currentUserId() {
        return AuthContextHolder.getRequired().userId();
    }

    private PatientProfileResponse toResponse(PatientProfile profile) {
        return new PatientProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDateOfBirth(),
                profile.getPhone(),
                profile.getCreatedAt()
        );
    }

    private DoctorProfileResponse toResponse(DoctorProfile profile) {
        return new DoctorProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getLicenseNo(),
                profile.getHospitalId(),
                profile.getSpecialty(),
                profile.getCreatedAt()
        );
    }

    private AgentProfileResponse toResponse(AgentProfile profile) {
        return new AgentProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getEmployeeNo(),
                profile.getDepartment(),
                profile.getCreatedAt()
        );
    }

    private UserProfileException notFound(String code, String message) {
        return new UserProfileException(code, message);
    }

    private UserProfileException forbidden(String message) {
        return new UserProfileException("FORBIDDEN", message);
    }
}
