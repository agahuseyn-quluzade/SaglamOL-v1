package az.saglamol.userprofile.controller;

import az.saglamol.userprofile.dto.response.UserProfileSummaryResponse;
import az.saglamol.userprofile.service.InternalProfileQueryService;
import az.saglamol.userprofile.service.InternalServiceSecretVerifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/profiles")
public class InternalProfileController {

    private final InternalProfileQueryService queryService;
    private final InternalServiceSecretVerifier secretVerifier;

    public InternalProfileController(
            InternalProfileQueryService queryService,
            InternalServiceSecretVerifier secretVerifier
    ) {
        this.queryService = queryService;
        this.secretVerifier = secretVerifier;
    }

    @GetMapping("/users/{iamUserId}/summary")
    public UserProfileSummaryResponse userSummary(
            @PathVariable UUID iamUserId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.summary(iamUserId);
    }

    @GetMapping("/patients/{patientProfileId}/exists")
    public boolean patientExists(
            @PathVariable UUID patientProfileId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.patientExists(patientProfileId);
    }

    @GetMapping("/doctors/{doctorProfileId}/exists")
    public boolean doctorExists(
            @PathVariable UUID doctorProfileId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.doctorExists(doctorProfileId);
    }

    @GetMapping("/hospitals/{hospitalId}/exists-active")
    public boolean hospitalActive(
            @PathVariable UUID hospitalId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.hospitalActive(hospitalId);
    }

    @GetMapping("/insurance-companies/{companyId}/exists-active")
    public boolean insuranceCompanyActive(
            @PathVariable UUID companyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.insuranceCompanyActive(companyId);
    }
}
