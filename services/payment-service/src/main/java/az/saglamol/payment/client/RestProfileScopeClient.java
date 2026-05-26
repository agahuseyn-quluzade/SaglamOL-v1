package az.saglamol.payment.client;

import az.saglamol.payment.client.dto.InsuranceScopeResponse;
import az.saglamol.payment.client.dto.UserProfileSummaryResponse;
import az.saglamol.payment.service.InternalServiceSecretVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class RestProfileScopeClient implements ProfileScopeClient {

    private final RestClient restClient;
    private final String internalSecret;

    public RestProfileScopeClient(
            RestClient.Builder restClientBuilder,
            @Value("${saglamol.clients.user-profile.base-url:http://localhost:8082}") String userProfileBaseUrl,
            @Value("${saglamol.security.internal-auth.secret:${INTERNAL_SERVICE_SECRET:dev-internal-secret}}") String internalSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(userProfileBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    @Override
    public UserProfileSummaryResponse userSummary(UUID iamUserId) {
        return restClient.get()
                .uri("/internal/v1/profiles/users/{iamUserId}/summary", iamUserId)
                .header(InternalServiceSecretVerifier.HEADER_NAME, internalSecret)
                .retrieve()
                .body(UserProfileSummaryResponse.class);
    }

    @Override
    public InsuranceScopeResponse insuranceScope(UUID iamUserId) {
        return restClient.get()
                .uri("/internal/v1/users/{iamUserId}/insurance-scope", iamUserId)
                .header(InternalServiceSecretVerifier.HEADER_NAME, internalSecret)
                .retrieve()
                .body(InsuranceScopeResponse.class);
    }
}
