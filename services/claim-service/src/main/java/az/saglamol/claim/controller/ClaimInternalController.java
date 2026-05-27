package az.saglamol.claim.controller;

import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.dto.response.ClaimSummaryResponse;
import az.saglamol.claim.service.ClaimInternalQueryService;
import az.saglamol.claim.service.InternalServiceSecretVerifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/claims")
public class ClaimInternalController {

    private final ClaimInternalQueryService queryService;
    private final InternalServiceSecretVerifier secretVerifier;

    public ClaimInternalController(ClaimInternalQueryService queryService, InternalServiceSecretVerifier secretVerifier) {
        this.queryService = queryService;
        this.secretVerifier = secretVerifier;
    }

    @GetMapping("/{claimId}")
    public ClaimResponse claim(
            @PathVariable UUID claimId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.claim(claimId);
    }

    @GetMapping("/{claimId}/summary")
    public ClaimSummaryResponse summary(
            @PathVariable UUID claimId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.summary(claimId);
    }

    @GetMapping("/by-patient/{patientProfileId}/summary")
    public Page<ClaimSummaryResponse> byPatient(
            @PathVariable UUID patientProfileId,
            Pageable pageable,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.byPatient(patientProfileId, pageable);
    }

    @GetMapping("/by-company/{companyId}/summary")
    public Page<ClaimSummaryResponse> byCompany(
            @PathVariable UUID companyId,
            Pageable pageable,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.byCompany(companyId, pageable);
    }

    @GetMapping("/by-hospital/{hospitalId}/summary")
    public Page<ClaimSummaryResponse> byHospital(
            @PathVariable UUID hospitalId,
            Pageable pageable,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.byHospital(hospitalId, pageable);
    }
}
