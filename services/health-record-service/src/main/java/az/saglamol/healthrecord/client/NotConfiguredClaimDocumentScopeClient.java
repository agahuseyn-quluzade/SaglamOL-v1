package az.saglamol.healthrecord.client;

import az.saglamol.common.security.AuthContext;
import az.saglamol.healthrecord.exception.HealthRecordException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnMissingBean(ClaimDocumentScopeClient.class)
public class NotConfiguredClaimDocumentScopeClient implements ClaimDocumentScopeClient {
    @Override
    public boolean canAccessClaimDocuments(AuthContext authContext, UUID claimId) {
        throw new HealthRecordException("INTERNAL_CLIENT_NOT_CONFIGURED", "Claim document scope client is not configured");
    }
}
