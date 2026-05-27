package az.saglamol.airisk.client;

import az.saglamol.airisk.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "fraud-detection-service", contextId = "aiRiskFraudInternalClient", configuration = FeignInternalClientConfig.class)
public interface FraudInternalClient {
    @GetMapping("/fraud/companies/{companyId}/summary")
    FraudSummaryResponse companySummary(@PathVariable("companyId") UUID companyId);
}
