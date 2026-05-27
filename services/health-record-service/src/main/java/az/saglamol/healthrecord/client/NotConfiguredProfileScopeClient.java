package az.saglamol.healthrecord.client;

import az.saglamol.healthrecord.exception.HealthRecordException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnMissingBean(ProfileScopeClient.class)
public class NotConfiguredProfileScopeClient implements ProfileScopeClient {
    @Override
    public UserProfileSummaryResponse userSummary(UUID iamUserId) {
        throw new HealthRecordException("INTERNAL_CLIENT_NOT_CONFIGURED", "Profile scope client is not configured");
    }
}
