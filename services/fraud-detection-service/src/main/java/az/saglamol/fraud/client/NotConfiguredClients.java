package az.saglamol.fraud.client;

import az.saglamol.fraud.exception.FraudException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

final class NotConfiguredClients {
    private NotConfiguredClients() {
    }

    static FraudException notConfigured(String name) {
        return new FraudException("INTERNAL_CLIENT_NOT_CONFIGURED", name + " is not configured");
    }
}

@Component
@ConditionalOnMissingBean(ClaimInternalClient.class)
class NotConfiguredClaimInternalClient implements ClaimInternalClient {
    @Override
    public ClaimDetailResponse getClaim(UUID claimId) {
        throw NotConfiguredClients.notConfigured("Claim internal client");
    }

    @Override
    public Page<ClaimSummaryResponse> claimsByCompany(UUID companyId, Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
}

@Component
@ConditionalOnMissingBean(HealthRecordInternalClient.class)
class NotConfiguredHealthRecordInternalClient implements HealthRecordInternalClient {
    @Override
    public List<MedicalDocumentSummaryResponse> documentsByClaim(UUID claimId) {
        throw NotConfiguredClients.notConfigured("Health record internal client");
    }

    @Override
    public MedicalDocumentHashResponse documentHash(UUID documentId) {
        throw NotConfiguredClients.notConfigured("Health record internal client");
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
@ConditionalOnMissingBean(ProfileScopeClient.class)
class NotConfiguredProfileScopeClient implements ProfileScopeClient {
    @Override
    public UserProfileSummaryResponse userSummary(UUID iamUserId) {
        throw NotConfiguredClients.notConfigured("Profile scope client");
    }
}
