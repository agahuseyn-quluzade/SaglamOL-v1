package az.saglamol.policy.client;

import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;
import az.saglamol.policy.dto.response.AgentCompanyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class RestProfileScopeClient implements ProfileScopeClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Service-Secret";

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
    public InsuranceScopeResponse insuranceScope(UUID iamUserId) {
        return restClient.get()
                .uri("/internal/v1/users/{iamUserId}/insurance-scope", iamUserId)
                .header(INTERNAL_SECRET_HEADER, internalSecret)
                .retrieve()
                .body(InsuranceScopeResponse.class);
    }

    @Override
    public UserProfileSummaryResponse userSummary(UUID iamUserId) {
        return restClient.get()
                .uri("/internal/v1/profiles/users/{iamUserId}/summary", iamUserId)
                .header(INTERNAL_SECRET_HEADER, internalSecret)
                .retrieve()
                .body(UserProfileSummaryResponse.class);
    }

    @Override
    public boolean patientExists(UUID patientProfileId) {
        Boolean exists = restClient.get()
                .uri("/internal/v1/profiles/patients/{patientProfileId}/exists", patientProfileId)
                .header(INTERNAL_SECRET_HEADER, internalSecret)
                .retrieve()
                .body(Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public AgentCompanyResponse agentCompany(UUID agentProfileId) {
        return restClient.get()
                .uri("/internal/v1/agents/{agentProfileId}/insurance-company", agentProfileId)
                .header(INTERNAL_SECRET_HEADER, internalSecret)
                .retrieve()
                .body(AgentCompanyResponse.class);
    }
}
