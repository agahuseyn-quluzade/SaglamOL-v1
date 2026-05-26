package az.saglamol.policy.controller;

import az.saglamol.policy.dto.request.EligibilityCheckRequest;
import az.saglamol.policy.dto.request.LimitReservationRequest;
import az.saglamol.policy.dto.request.ReleaseReservationRequest;
import az.saglamol.policy.dto.response.EligibilityCheckResponse;
import az.saglamol.policy.dto.response.PolicyLimitReservationResponse;
import az.saglamol.policy.dto.response.PolicyResponse;
import az.saglamol.policy.service.EligibilityService;
import az.saglamol.policy.service.InternalServiceSecretVerifier;
import az.saglamol.policy.service.PolicyLimitService;
import az.saglamol.policy.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/policies")
public class InternalPolicyController {

    private final PolicyService policyService;
    private final EligibilityService eligibilityService;
    private final PolicyLimitService policyLimitService;
    private final InternalServiceSecretVerifier secretVerifier;

    public InternalPolicyController(
            PolicyService policyService,
            EligibilityService eligibilityService,
            PolicyLimitService policyLimitService,
            InternalServiceSecretVerifier secretVerifier
    ) {
        this.policyService = policyService;
        this.eligibilityService = eligibilityService;
        this.policyLimitService = policyLimitService;
        this.secretVerifier = secretVerifier;
    }

    @GetMapping("/{policyId}")
    public PolicyResponse getPolicy(
            @PathVariable UUID policyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyService.getPolicyInternal(policyId);
    }

    @GetMapping("/{policyId}/active")
    public boolean isPolicyActive(
            @PathVariable UUID policyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyService.isPolicyActive(policyId);
    }

    @PostMapping("/eligibility-check")
    public EligibilityCheckResponse eligibilityCheck(
            @Valid @RequestBody EligibilityCheckRequest request,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return eligibilityService.checkEligibility(request);
    }

    @PostMapping("/{policyId}/limit-reservations")
    public PolicyLimitReservationResponse reserveLimit(
            @PathVariable UUID policyId,
            @Valid @RequestBody LimitReservationRequest request,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyLimitService.reserveLimit(policyId, request.claimId(), request.companyId(), request.amount());
    }

    @PutMapping("/limit-reservations/{reservationId}/confirm")
    public PolicyLimitReservationResponse confirmReservation(
            @PathVariable UUID reservationId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyLimitService.commitReservation(reservationId);
    }

    @PutMapping("/limit-reservations/{reservationId}/release")
    public PolicyLimitReservationResponse releaseReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody(required = false) ReleaseReservationRequest request,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyLimitService.releaseReservation(reservationId, request == null ? null : request.reason());
    }

    @PutMapping("/{policyId}/activate-after-payment")
    public PolicyResponse activateAfterPayment(
            @PathVariable UUID policyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return policyService.activatePolicy(policyId);
    }
}
