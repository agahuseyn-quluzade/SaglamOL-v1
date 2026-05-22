package az.saglamol.userprofile.controller;

import az.saglamol.common.security.InternalAuthHeaders;
import az.saglamol.userprofile.dto.request.AssignDoctorToHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalBranchRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalStaffRequest;
import az.saglamol.userprofile.dto.response.DoctorHospitalAssignmentResponse;
import az.saglamol.userprofile.dto.response.HospitalBranchResponse;
import az.saglamol.userprofile.dto.response.HospitalResponse;
import az.saglamol.userprofile.dto.response.HospitalStaffResponse;
import az.saglamol.userprofile.security.ProviderAccessService;
import az.saglamol.userprofile.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;
    private final ProviderAccessService providerAccessService;

    public HospitalController(HospitalService hospitalService, ProviderAccessService providerAccessService) {
        this.hospitalService = hospitalService;
        this.providerAccessService = providerAccessService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse createHospital(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @Valid @RequestBody CreateHospitalRequest request
    ) {
        providerAccessService.requireHospitalWrite(roles);
        return hospitalService.createHospital(request);
    }

    @GetMapping
    public List<HospitalResponse> hospitals(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles
    ) {
        providerAccessService.requireHospitalRead(roles);
        return hospitalService.hospitals();
    }

    @GetMapping("/{hospitalId}")
    public HospitalResponse hospital(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId
    ) {
        providerAccessService.requireHospitalRead(roles);
        return hospitalService.hospital(hospitalId);
    }

    @PostMapping("/{hospitalId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalBranchResponse createBranch(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateHospitalBranchRequest request
    ) {
        providerAccessService.requireHospitalWrite(roles);
        return hospitalService.createBranch(hospitalId, request);
    }

    @GetMapping("/{hospitalId}/branches")
    public List<HospitalBranchResponse> branches(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId
    ) {
        providerAccessService.requireHospitalRead(roles);
        return hospitalService.branches(hospitalId);
    }

    @PostMapping("/{hospitalId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalStaffResponse createStaff(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateHospitalStaffRequest request
    ) {
        providerAccessService.requireHospitalWrite(roles);
        return hospitalService.createStaff(hospitalId, request);
    }

    @GetMapping("/{hospitalId}/staff")
    public List<HospitalStaffResponse> staff(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId
    ) {
        providerAccessService.requireHospitalRead(roles);
        return hospitalService.staff(hospitalId);
    }

    @PostMapping("/{hospitalId}/doctors/{doctorProfileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorHospitalAssignmentResponse assignDoctor(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId,
            @PathVariable UUID doctorProfileId,
            @Valid @RequestBody AssignDoctorToHospitalRequest request
    ) {
        providerAccessService.requireHospitalWrite(roles);
        return hospitalService.assignDoctor(hospitalId, doctorProfileId, request);
    }

    @GetMapping("/{hospitalId}/doctors")
    public List<DoctorHospitalAssignmentResponse> doctorAssignments(
            @RequestHeader(value = InternalAuthHeaders.USER_ROLES, required = false) String roles,
            @PathVariable UUID hospitalId
    ) {
        providerAccessService.requireHospitalRead(roles);
        return hospitalService.doctorAssignments(hospitalId);
    }
}
