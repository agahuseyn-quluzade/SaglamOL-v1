package az.saglamol.userprofile.controller;

import az.saglamol.userprofile.dto.request.AssignDoctorToHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalBranchRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalStaffRequest;
import az.saglamol.userprofile.dto.request.UpdateHospitalRequest;
import az.saglamol.userprofile.dto.response.DoctorHospitalAssignmentResponse;
import az.saglamol.userprofile.dto.response.HospitalBranchResponse;
import az.saglamol.userprofile.dto.response.HospitalResponse;
import az.saglamol.userprofile.dto.response.HospitalStaffResponse;
import az.saglamol.userprofile.entity.HospitalStatus;
import az.saglamol.userprofile.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalResponse createHospital(
            @Valid @RequestBody CreateHospitalRequest request
    ) {
        return hospitalService.createHospital(request);
    }

    @GetMapping
    public Page<HospitalResponse> hospitals(
            @RequestParam(required = false) HospitalStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return hospitalService.searchHospitals(status, name, email, city, pageable);
    }

    @GetMapping("/{hospitalId}")
    public HospitalResponse hospital(
            @PathVariable UUID hospitalId
    ) {
        return hospitalService.hospital(hospitalId);
    }

    @PutMapping("/{hospitalId}")
    public HospitalResponse updateHospital(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody UpdateHospitalRequest request
    ) {
        return hospitalService.updateHospital(hospitalId, request);
    }

    @PatchMapping("/{hospitalId}/status")
    public HospitalResponse changeStatus(
            @PathVariable UUID hospitalId,
            @RequestParam HospitalStatus newStatus
    ) {
        return hospitalService.changeStatus(hospitalId, newStatus);
    }

    @PostMapping("/{hospitalId}/branches")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalBranchResponse createBranch(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateHospitalBranchRequest request
    ) {
        return hospitalService.createBranch(hospitalId, request);
    }

    @GetMapping("/{hospitalId}/branches")
    public List<HospitalBranchResponse> branches(
            @PathVariable UUID hospitalId
    ) {
        return hospitalService.branches(hospitalId);
    }

    @PostMapping("/{hospitalId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public HospitalStaffResponse createStaff(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateHospitalStaffRequest request
    ) {
        return hospitalService.createStaff(hospitalId, request);
    }

    @GetMapping("/{hospitalId}/staff")
    public List<HospitalStaffResponse> staff(
            @PathVariable UUID hospitalId
    ) {
        return hospitalService.staff(hospitalId);
    }

    @PostMapping("/{hospitalId}/doctors/{doctorProfileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorHospitalAssignmentResponse assignDoctor(
            @PathVariable UUID hospitalId,
            @PathVariable UUID doctorProfileId,
            @Valid @RequestBody AssignDoctorToHospitalRequest request
    ) {
        return hospitalService.assignDoctor(hospitalId, doctorProfileId, request);
    }

    @GetMapping("/{hospitalId}/doctors")
    public List<DoctorHospitalAssignmentResponse> doctorAssignments(
            @PathVariable UUID hospitalId
    ) {
        return hospitalService.doctorAssignments(hospitalId);
    }
}
