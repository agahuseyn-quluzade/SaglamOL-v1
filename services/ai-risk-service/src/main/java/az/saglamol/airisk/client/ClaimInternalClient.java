package az.saglamol.airisk.client;

import az.saglamol.airisk.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "claim-service", contextId = "aiRiskClaimInternalClient", configuration = FeignInternalClientConfig.class)
public interface ClaimInternalClient {
    @GetMapping("/internal/v1/claims/{claimId}")
    ClaimDetailResponse getClaim(@PathVariable("claimId") UUID claimId);
}
