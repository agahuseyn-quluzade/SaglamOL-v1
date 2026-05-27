package az.saglamol.claim.client;

import az.saglamol.claim.client.dto.EligibilityCheckRequest;
import az.saglamol.claim.client.dto.EligibilityCheckResponse;
import az.saglamol.claim.client.dto.LimitReservationRequest;
import az.saglamol.claim.client.dto.PolicyDetailResponse;
import az.saglamol.claim.client.dto.PolicyLimitReservationResponse;
import az.saglamol.claim.client.dto.ReleaseReservationRequest;
import az.saglamol.claim.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "policy-service",
        path = "/internal/v1/policies",
        configuration = FeignInternalClientConfig.class
)
public interface PolicyInternalClient {
    @GetMapping("/{policyId}")
    PolicyDetailResponse getPolicy(@PathVariable UUID policyId);

    @PostMapping("/eligibility-check")
    EligibilityCheckResponse checkEligibility(@RequestBody EligibilityCheckRequest request);

    @PostMapping("/{policyId}/limit-reservations")
    PolicyLimitReservationResponse reserveLimit(@PathVariable UUID policyId, @RequestBody LimitReservationRequest request);

    @PutMapping("/limit-reservations/{reservationId}/confirm")
    PolicyLimitReservationResponse confirmReservation(@PathVariable UUID reservationId);

    @PutMapping("/limit-reservations/{reservationId}/release")
    PolicyLimitReservationResponse releaseReservation(@PathVariable UUID reservationId, @RequestBody ReleaseReservationRequest request);
}
