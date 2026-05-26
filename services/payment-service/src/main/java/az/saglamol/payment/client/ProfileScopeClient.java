package az.saglamol.payment.client;

import az.saglamol.payment.client.dto.InsuranceScopeResponse;
import az.saglamol.payment.client.dto.UserProfileSummaryResponse;

import java.util.UUID;

public interface ProfileScopeClient {

    UserProfileSummaryResponse userSummary(UUID iamUserId);

    InsuranceScopeResponse insuranceScope(UUID iamUserId);
}
