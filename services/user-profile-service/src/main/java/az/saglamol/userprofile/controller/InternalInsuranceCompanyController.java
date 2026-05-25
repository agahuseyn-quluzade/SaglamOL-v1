package az.saglamol.userprofile.controller;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextResolver;
import az.saglamol.userprofile.dto.response.AccessCheckResponse;
import az.saglamol.userprofile.dto.response.AgentCompanyResponse;
import az.saglamol.userprofile.dto.response.InsuranceScopeResponse;
import az.saglamol.userprofile.service.InternalInsuranceCompanyQueryService;
import az.saglamol.userprofile.service.InternalServiceSecretVerifier;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1")
public class InternalInsuranceCompanyController {

    private final InternalInsuranceCompanyQueryService queryService;
    private final InternalServiceSecretVerifier secretVerifier;
    private final AuthContextResolver authContextResolver;

    public InternalInsuranceCompanyController(
            InternalInsuranceCompanyQueryService queryService,
            InternalServiceSecretVerifier secretVerifier,
            AuthContextResolver authContextResolver
    ) {
        this.queryService = queryService;
        this.secretVerifier = secretVerifier;
        this.authContextResolver = authContextResolver;
    }

    @GetMapping("/insurance-companies/{companyId}/exists-active")
    public boolean existsActive(
            @PathVariable UUID companyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.existsActive(companyId);
    }

    @GetMapping("/users/{iamUserId}/insurance-scope")
    public InsuranceScopeResponse insuranceScope(
            @PathVariable UUID iamUserId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.insuranceScope(iamUserId);
    }

    @GetMapping("/agents/{agentProfileId}/insurance-company")
    public AgentCompanyResponse agentCompany(
            @PathVariable UUID agentProfileId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret
    ) {
        secretVerifier.verify(secret);
        return queryService.agentCompany(agentProfileId);
    }

    @GetMapping("/insurance-companies/{companyId}/access/current")
    public AccessCheckResponse currentAccess(
            @PathVariable UUID companyId,
            @RequestHeader(value = InternalServiceSecretVerifier.HEADER_NAME, required = false) String secret,
            HttpServletRequest request
    ) {
        secretVerifier.verify(secret);
        AuthContext authContext = authContextResolver.resolve(request);
        return queryService.accessForCurrent(companyId, authContext);
    }
}
