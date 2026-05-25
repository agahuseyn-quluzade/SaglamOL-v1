package az.saglamol.userprofile.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.exception.ErrorResponse;
import az.saglamol.common.security.InternalAuthHeaders;
import az.saglamol.userprofile.dto.request.UpsertAgentProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertDoctorProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.dto.response.DoctorProfileResponse;
import az.saglamol.userprofile.dto.response.PatientProfileResponse;
import az.saglamol.userprofile.entity.ProfileStatus;
import az.saglamol.userprofile.service.AgentCompanyService;
import az.saglamol.userprofile.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "User Profiles", description = "Patient, doctor and agent profile APIs")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Validation or business error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid gateway identity headers",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden by role or ownership",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Duplicate profile or unique field",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AgentCompanyService agentCompanyService;

    public UserProfileController(UserProfileService userProfileService, AgentCompanyService agentCompanyService) {
        this.userProfileService = userProfileService;
        this.agentCompanyService = agentCompanyService;
    }

    @PostMapping("/patients")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create patient profile", description = "Creates a patient profile for the authenticated AuthContext user.")
    @GatewayIdentityHeaders
    public PatientProfileResponse createPatientProfile(@Valid @RequestBody UpsertPatientProfileRequest request) {
        return userProfileService.createPatientProfile(request);
    }

    @GetMapping("/patients/me")
    @Operation(summary = "Get my patient profile", description = "Returns the patient profile owned by the authenticated AuthContext user.")
    @GatewayIdentityHeaders
    public PatientProfileResponse myPatientProfile() {
        return userProfileService.myPatientProfile();
    }

    @GetMapping("/patients/{id}")
    @Operation(summary = "Get patient profile", description = "Returns a patient profile if the authenticated user owns it or has read access.")
    @GatewayIdentityHeaders
    public PatientProfileResponse patient(@PathVariable UUID id) {
        return userProfileService.patient(id);
    }

    @PutMapping("/patients/{id}")
    @Operation(summary = "Update patient profile", description = "Updates a patient profile if the authenticated user owns it or is ADMIN.")
    @GatewayIdentityHeaders
    public PatientProfileResponse updatePatientProfile(@PathVariable UUID id,
                                                       @Valid @RequestBody UpsertPatientProfileRequest request) {
        return userProfileService.updatePatientProfile(id, request);
    }

    @GetMapping("/patients/search")
    @Operation(summary = "Search patient profiles", description = "Paginated patient search for ADMIN and AGENT roles.")
    @GatewayIdentityHeaders
    public Page<PatientProfileResponse> searchPatients(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProfileStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return userProfileService.searchPatients(query, status, name, email, pageable);
    }

    @PostMapping("/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create doctor profile", description = "Creates a doctor profile for the authenticated AuthContext user. HOSPITAL_ADMIN cannot create doctor profiles.")
    @GatewayIdentityHeaders
    public DoctorProfileResponse createDoctorProfile(@Valid @RequestBody UpsertDoctorProfileRequest request) {
        return userProfileService.createDoctorProfile(request);
    }

    @GetMapping("/doctors/me")
    @Operation(summary = "Get my doctor profile", description = "Returns the doctor profile owned by the authenticated AuthContext user.")
    @GatewayIdentityHeaders
    public DoctorProfileResponse myDoctorProfile() {
        return userProfileService.myDoctorProfile();
    }

    @GetMapping("/doctors/{id}")
    @Operation(summary = "Get doctor profile", description = "Returns a doctor profile if the authenticated user owns it or has provider read scope.")
    @GatewayIdentityHeaders
    public DoctorProfileResponse doctor(@PathVariable UUID id) {
        return userProfileService.doctor(id);
    }

    @PutMapping("/doctors/{id}")
    @Operation(summary = "Update doctor profile", description = "Updates a doctor profile if the authenticated user owns it or is ADMIN.")
    @GatewayIdentityHeaders
    public DoctorProfileResponse updateDoctorProfile(@PathVariable UUID id,
                                                     @Valid @RequestBody UpsertDoctorProfileRequest request) {
        return userProfileService.updateDoctorProfile(id, request);
    }

    @GetMapping("/doctors/search")
    @Operation(summary = "Search doctor profiles", description = "Paginated doctor search for ADMIN, AGENT and hospital provider roles.")
    @GatewayIdentityHeaders
    public Page<DoctorProfileResponse> searchDoctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProfileStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) UUID hospitalId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return userProfileService.searchDoctors(query, status, name, email, specialty, hospitalId, pageable);
    }

    @PostMapping("/agents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create agent profile", description = "Creates an agent profile for the authenticated AuthContext user.")
    @GatewayIdentityHeaders
    public AgentProfileResponse createAgentProfile(@Valid @RequestBody UpsertAgentProfileRequest request) {
        return userProfileService.createAgentProfile(request);
    }

    @GetMapping("/agents/me")
    @Operation(summary = "Get my agent profile", description = "Returns the agent profile owned by the authenticated AuthContext user.")
    @GatewayIdentityHeaders
    public AgentProfileResponse myAgentProfile() {
        return userProfileService.myAgentProfile();
    }

    @GetMapping("/agents/{id}")
    @Operation(summary = "Get agent profile", description = "Returns an agent profile if the authenticated user owns it or is ADMIN.")
    @GatewayIdentityHeaders
    public AgentProfileResponse agent(@PathVariable UUID id) {
        return userProfileService.agent(id);
    }

    @PutMapping("/agents/{id}")
    @Operation(summary = "Update agent profile", description = "Updates an agent profile if the authenticated user owns it or is ADMIN.")
    @GatewayIdentityHeaders
    public AgentProfileResponse updateAgentProfile(@PathVariable UUID id,
                                                   @Valid @RequestBody UpsertAgentProfileRequest request) {
        return userProfileService.updateAgentProfile(id, request);
    }

    @GetMapping("/agents/search")
    @Operation(summary = "Search agent profiles", description = "Paginated agent search for ADMIN role.")
    @GatewayIdentityHeaders
    public Page<AgentProfileResponse> searchAgents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProfileStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UUID companyId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return userProfileService.searchAgents(query, status, name, email, companyId, pageable);
    }

    @PatchMapping("/agents/{agentProfileId}/insurance-company/{companyId}")
    @Operation(summary = "Link agent to insurance company", description = "Links an existing agent profile to an insurance company.")
    @GatewayIdentityHeaders
    public AgentCompanyResponse linkAgentToCompany(
            @PathVariable UUID agentProfileId,
            @PathVariable UUID companyId
    ) {
        return agentCompanyService.linkAgentToCompany(agentProfileId, companyId, AuthContextHolder.getRequired());
    }

    @GetMapping("/agents/by-company/{companyId}")
    @Operation(summary = "Get agents by company", description = "Returns paginated agents scoped to an insurance company.")
    @GatewayIdentityHeaders
    public Page<AgentProfileResponse> agentsByCompany(
            @PathVariable UUID companyId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return agentCompanyService.getAgentsByCompany(companyId, AuthContextHolder.getRequired(), pageable);
    }

    @Parameter(name = InternalAuthHeaders.USER_ID, in = ParameterIn.HEADER, required = true,
            description = "Authenticated IAM user id forwarded by API Gateway")
    @Parameter(name = InternalAuthHeaders.USER_ROLES, in = ParameterIn.HEADER, required = true,
            description = "Comma-separated authenticated roles forwarded by API Gateway")
    @Parameter(name = InternalAuthHeaders.CORRELATION_ID, in = ParameterIn.HEADER,
            description = "Correlation id forwarded by API Gateway")
    private @interface GatewayIdentityHeaders {
    }
}
