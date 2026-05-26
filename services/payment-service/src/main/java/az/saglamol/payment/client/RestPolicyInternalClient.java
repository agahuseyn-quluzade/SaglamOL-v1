package az.saglamol.payment.client;

import az.saglamol.payment.service.InternalServiceSecretVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class RestPolicyInternalClient implements PolicyInternalClient {

    private final RestClient restClient;
    private final String internalSecret;

    public RestPolicyInternalClient(
            RestClient.Builder restClientBuilder,
            @Value("${saglamol.clients.policy.base-url:http://localhost:8084}") String policyBaseUrl,
            @Value("${saglamol.security.internal-auth.secret:${INTERNAL_SERVICE_SECRET:dev-internal-secret}}") String internalSecret
    ) {
        this.restClient = restClientBuilder.baseUrl(policyBaseUrl).build();
        this.internalSecret = internalSecret;
    }

    @Override
    public void activateAfterPayment(UUID policyId) {
        restClient.put()
                .uri("/internal/v1/policies/{policyId}/activate-after-payment", policyId)
                .header(InternalServiceSecretVerifier.HEADER_NAME, internalSecret)
                .retrieve()
                .toBodilessEntity();
    }
}
