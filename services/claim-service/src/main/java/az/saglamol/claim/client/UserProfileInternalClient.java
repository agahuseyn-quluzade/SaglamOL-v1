package az.saglamol.claim.client;

import az.saglamol.claim.client.dto.UserProfileSummaryResponse;
import az.saglamol.claim.config.FeignInternalClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-profile-service",
        path = "/internal/v1/profiles",
        configuration = FeignInternalClientConfig.class
)
public interface UserProfileInternalClient extends ProfileInternalClient {

    @Override
    @GetMapping("/users/{iamUserId}/summary")
    UserProfileSummaryResponse userSummary(@PathVariable UUID iamUserId);
}
