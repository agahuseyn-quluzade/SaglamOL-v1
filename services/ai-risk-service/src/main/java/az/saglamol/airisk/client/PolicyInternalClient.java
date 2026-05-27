package az.saglamol.airisk.client;

import az.saglamol.airisk.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "policy-service", contextId = "aiRiskPolicyInternalClient", configuration = FeignInternalClientConfig.class)
public interface PolicyInternalClient {
    @GetMapping("/internal/v1/policies/{policyId}")
    PolicyDetailResponse getPolicy(@PathVariable("policyId") UUID policyId);
}
