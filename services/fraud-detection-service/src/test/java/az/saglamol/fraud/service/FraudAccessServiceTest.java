package az.saglamol.fraud.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.fraud.client.ProfileScopeClient;
import az.saglamol.fraud.client.UserProfileSummaryResponse;
import az.saglamol.fraud.exception.FraudException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FraudAccessServiceTest {

    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final FraudAccessService service = new FraudAccessService(profileScopeClient);

    @Test
    void patientForbidden() {
        UUID userId = UUID.randomUUID();

        FraudException exception = assertThrows(FraudException.class,
                () -> service.requireCanRunCheck(auth(userId, RoleConstants.PATIENT), UUID.randomUUID(), null));

        assertEquals("PATIENT_FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void agentCanAccessOwnCompanyOnly() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, companyId, null));

        service.requireCanRunCheck(auth(userId, RoleConstants.AGENT), companyId, null);

        FraudException exception = assertThrows(FraudException.class,
                () -> service.requireCanRunCheck(auth(userId, RoleConstants.AGENT), UUID.randomUUID(), null));
        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    private AuthContext auth(UUID userId, String role) {
        return new AuthContext(userId, List.of(role), "corr", Map.of());
    }

    private UserProfileSummaryResponse summary(UUID userId, UUID companyId, UUID hospitalId) {
        return new UserProfileSummaryResponse(userId, null, null, UUID.randomUUID(), companyId,
                null, hospitalId, null, false, false, true, false, false);
    }
}
