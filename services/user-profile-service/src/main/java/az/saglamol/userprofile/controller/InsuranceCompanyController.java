package az.saglamol.userprofile.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyStaffRequest;
import az.saglamol.userprofile.dto.request.UpdateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.response.InsuranceCompanyResponse;
import az.saglamol.userprofile.dto.response.InsuranceCompanyStaffResponse;
import az.saglamol.userprofile.entity.InsuranceCompanyStaffStatus;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.service.InsuranceCompanyService;
import az.saglamol.userprofile.service.InsuranceCompanyStaffService;
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
@RequestMapping("/api/v1/insurance-companies")
public class InsuranceCompanyController {

    private final InsuranceCompanyService companyService;
    private final InsuranceCompanyStaffService staffService;

    public InsuranceCompanyController(
            InsuranceCompanyService companyService,
            InsuranceCompanyStaffService staffService
    ) {
        this.companyService = companyService;
        this.staffService = staffService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsuranceCompanyResponse createCompany(@Valid @RequestBody CreateInsuranceCompanyRequest request) {
        return companyService.createCompany(AuthContextHolder.getRequired(), request);
    }

    @GetMapping
    public Page<InsuranceCompanyResponse> companies(
            @RequestParam(required = false) InsuranceCompanyStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return companyService.getAllCompanies(status, pageable, AuthContextHolder.getRequired());
    }

    @GetMapping("/{id}")
    public InsuranceCompanyResponse company(@PathVariable UUID id) {
        return companyService.getCompany(id, AuthContextHolder.getRequired());
    }

    @PutMapping("/{id}")
    public InsuranceCompanyResponse updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInsuranceCompanyRequest request
    ) {
        return companyService.updateCompany(id, AuthContextHolder.getRequired(), request);
    }

    @PatchMapping("/{id}/status")
    public InsuranceCompanyResponse changeStatus(
            @PathVariable UUID id,
            @RequestParam InsuranceCompanyStatus newStatus
    ) {
        return companyService.changeStatus(id, AuthContextHolder.getRequired(), newStatus);
    }

    @PostMapping("/{companyId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public InsuranceCompanyStaffResponse createStaff(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateInsuranceCompanyStaffRequest request
    ) {
        return staffService.createStaff(companyId, AuthContextHolder.getRequired(), request);
    }

    @GetMapping("/{companyId}/staff")
    public Page<InsuranceCompanyStaffResponse> staff(
            @PathVariable UUID companyId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return staffService.getStaffByCompany(companyId, AuthContextHolder.getRequired(), pageable);
    }

    @GetMapping("/{companyId}/staff/{staffId}")
    public InsuranceCompanyStaffResponse staffById(
            @PathVariable UUID companyId,
            @PathVariable UUID staffId
    ) {
        return staffService.getStaffById(companyId, staffId, AuthContextHolder.getRequired());
    }

    @PatchMapping("/{companyId}/staff/{staffId}/status")
    public InsuranceCompanyStaffResponse changeStaffStatus(
            @PathVariable UUID companyId,
            @PathVariable UUID staffId,
            @RequestParam InsuranceCompanyStaffStatus newStatus
    ) {
        return staffService.changeStaffStatus(companyId, staffId, AuthContextHolder.getRequired(), newStatus);
    }
}
