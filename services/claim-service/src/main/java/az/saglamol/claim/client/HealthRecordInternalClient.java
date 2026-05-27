package az.saglamol.claim.client;

import az.saglamol.claim.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "health-record-service",
        path = "/internal/v1/health-records",
        configuration = FeignInternalClientConfig.class
)
public interface HealthRecordInternalClient {

    @GetMapping("/documents/{documentId}/exists")
    boolean documentExists(@PathVariable UUID documentId);
}
