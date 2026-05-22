package az.saglamol.userprofile.controller;

import az.saglamol.userprofile.dto.request.UpsertAgentProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertDoctorProfileRequest;
import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import az.saglamol.userprofile.dto.response.AgentProfileResponse;
import az.saglamol.userprofile.dto.response.DoctorProfileResponse;
import az.saglamol.userprofile.dto.response.PatientProfileResponse;
import az.saglamol.userprofile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/patients/me")
    @ResponseStatus(HttpStatus.CREATED)
    public PatientProfileResponse createMyPatientProfile(@Valid @RequestBody UpsertPatientProfileRequest request) {
        return userProfileService.createMyPatientProfile(request);
    }

    @GetMapping("/patients/me")
    public PatientProfileResponse myPatientProfile() {
        return userProfileService.myPatientProfile();
    }

    @PatchMapping("/patients/me")
    public PatientProfileResponse updateMyPatientProfile(@Valid @RequestBody UpsertPatientProfileRequest request) {
        return userProfileService.updateMyPatientProfile(request);
    }

    @GetMapping("/patients")
    public List<PatientProfileResponse> patients() {
        return userProfileService.patients();
    }

    @GetMapping("/patients/{profileId}")
    public PatientProfileResponse patient(@PathVariable UUID profileId) {
        return userProfileService.patient(profileId);
    }

    @PostMapping("/doctors/me")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorProfileResponse createMyDoctorProfile(@Valid @RequestBody UpsertDoctorProfileRequest request) {
        return userProfileService.createMyDoctorProfile(request);
    }

    @GetMapping("/doctors/me")
    public DoctorProfileResponse myDoctorProfile() {
        return userProfileService.myDoctorProfile();
    }

    @PatchMapping("/doctors/me")
    public DoctorProfileResponse updateMyDoctorProfile(@Valid @RequestBody UpsertDoctorProfileRequest request) {
        return userProfileService.updateMyDoctorProfile(request);
    }

    @GetMapping("/doctors")
    public List<DoctorProfileResponse> doctors() {
        return userProfileService.doctors();
    }

    @GetMapping("/doctors/{profileId}")
    public DoctorProfileResponse doctor(@PathVariable UUID profileId) {
        return userProfileService.doctor(profileId);
    }

    @PostMapping("/agents/me")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentProfileResponse createMyAgentProfile(@Valid @RequestBody UpsertAgentProfileRequest request) {
        return userProfileService.createMyAgentProfile(request);
    }

    @GetMapping("/agents/me")
    public AgentProfileResponse myAgentProfile() {
        return userProfileService.myAgentProfile();
    }

    @PatchMapping("/agents/me")
    public AgentProfileResponse updateMyAgentProfile(@Valid @RequestBody UpsertAgentProfileRequest request) {
        return userProfileService.updateMyAgentProfile(request);
    }

    @GetMapping("/agents")
    public List<AgentProfileResponse> agents() {
        return userProfileService.agents();
    }

    @GetMapping("/agents/{profileId}")
    public AgentProfileResponse agent(@PathVariable UUID profileId) {
        return userProfileService.agent(profileId);
    }
}
