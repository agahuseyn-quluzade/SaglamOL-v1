package az.saglamol.policy.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.policy.dto.request.EligibilityCheckRequest;
import az.saglamol.policy.dto.request.IssuePolicyRequest;
import az.saglamol.policy.dto.request.PolicyStatusChangeRequest;
import az.saglamol.policy.dto.response.EligibilityCheckResponse;
import az.saglamol.policy.dto.response.PolicyResponse;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.service.EligibilityService;
import az.saglamol.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "Policies", description = "Policy issue, eligibility and lifecycle endpoints")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful policy operation")
public class PolicyController {

    private final PolicyService policyService;
    private final EligibilityService eligibilityService;

    public PolicyController(PolicyService policyService, EligibilityService eligibilityService) {
        this.policyService = policyService;
        this.eligibilityService = eligibilityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Issue policy")
    public PolicyResponse issuePolicy(@Valid @RequestBody IssuePolicyRequest request) {
        return policyService.issuePolicy(AuthContextHolder.getRequired(), request);
    }

    @GetMapping("/{policyId}")
    @Operation(summary = "Get policy by ID")
    public PolicyResponse getPolicy(@PathVariable UUID policyId) {
        return policyService.getPolicy(policyId, AuthContextHolder.getRequired());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current patient's policies")
    public List<PolicyResponse> getMyPolicies() {
        return policyService.getMyPolicies(AuthContextHolder.getRequired());
    }

    @GetMapping
    @Operation(summary = "Search policies")
    public Page<PolicyResponse> searchPolicies(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) UUID patientProfileId,
            @RequestParam(required = false) PolicyStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return policyService.searchPolicies(companyId, patientProfileId, status, pageable, AuthContextHolder.getRequired());
    }

    @PostMapping("/eligibility-check")
    @Operation(summary = "Check policy eligibility")
    public EligibilityCheckResponse eligibilityCheck(@Valid @RequestBody EligibilityCheckRequest request) {
        return eligibilityService.checkEligibility(request);
    }

    @PatchMapping("/{policyId}/cancel")
    public PolicyResponse cancelPolicy(
            @PathVariable UUID policyId,
            @Valid @RequestBody PolicyStatusChangeRequest request
    ) {
        return policyService.cancelPolicy(policyId, AuthContextHolder.getRequired(), request.reason());
    }

    @PatchMapping("/{policyId}/suspend")
    public PolicyResponse suspendPolicy(
            @PathVariable UUID policyId,
            @Valid @RequestBody PolicyStatusChangeRequest request
    ) {
        return policyService.suspendPolicy(policyId, AuthContextHolder.getRequired(), request.reason());
    }
}
