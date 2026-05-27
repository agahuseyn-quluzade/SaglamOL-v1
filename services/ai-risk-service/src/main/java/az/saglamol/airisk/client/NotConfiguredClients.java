package az.saglamol.airisk.client;

import az.saglamol.airisk.exception.AiRiskException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

final class NotConfiguredClients {
    private NotConfiguredClients() {
    }

    static AiRiskException notConfigured(String name) {
        return new AiRiskException("INTERNAL_CLIENT_NOT_CONFIGURED", name + " is not configured");
    }
}

@Component
@ConditionalOnMissingBean(ClaimInternalClient.class)
class NotConfiguredClaimInternalClient implements ClaimInternalClient {
    @Override
    public ClaimDetailResponse getClaim(UUID claimId) {
        throw NotConfiguredClients.notConfigured("Claim internal client");
    }
}

@Component
@ConditionalOnMissingBean(PolicyInternalClient.class)
class NotConfiguredPolicyInternalClient implements PolicyInternalClient {
    @Override
    public PolicyDetailResponse getPolicy(UUID policyId) {
        return null;
    }
}

@Component
@ConditionalOnMissingBean(FraudInternalClient.class)
class NotConfiguredFraudInternalClient implements FraudInternalClient {
    @Override
    public FraudSummaryResponse companySummary(UUID companyId) {
        return null;
    }
}

@Component
@ConditionalOnMissingBean(ProfileScopeClient.class)
class NotConfiguredProfileScopeClient implements ProfileScopeClient {
    @Override
    public UserProfileSummaryResponse userSummary(UUID iamUserId) {
        throw NotConfiguredClients.notConfigured("Profile scope client");
    }
}
