package az.saglamol.fraud.client;

import az.saglamol.fraud.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-profile-service", contextId = "fraudProfileScopeClient", configuration = FeignInternalClientConfig.class)
public interface ProfileScopeClient {
    @GetMapping("/internal/v1/profiles/users/{iamUserId}/summary")
    UserProfileSummaryResponse userSummary(@PathVariable("iamUserId") UUID iamUserId);
}
