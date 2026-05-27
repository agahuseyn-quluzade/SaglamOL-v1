package az.saglamol.fraud.client;

import az.saglamol.fraud.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "claim-service", contextId = "fraudClaimInternalClient", configuration = FeignInternalClientConfig.class)
public interface ClaimInternalClient {
    @GetMapping("/internal/v1/claims/{claimId}")
    ClaimDetailResponse getClaim(@PathVariable("claimId") UUID claimId);

    @GetMapping("/internal/v1/claims/by-company/{companyId}/summary")
    Page<ClaimSummaryResponse> claimsByCompany(@PathVariable("companyId") UUID companyId, Pageable pageable);
}
