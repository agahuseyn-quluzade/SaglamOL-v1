package az.saglamol.userprofile.controller;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextResolver;
import az.saglamol.common.security.InternalAuthHeaders;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.dto.response.AccessCheckResponse;
import az.saglamol.userprofile.service.InternalInsuranceCompanyQueryService;
import az.saglamol.userprofile.service.InternalServiceSecretVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalInsuranceCompanyControllerTest {

    private final InternalInsuranceCompanyQueryService queryService = mock(InternalInsuranceCompanyQueryService.class);
    private final InternalServiceSecretVerifier secretVerifier = new InternalServiceSecretVerifier("secret");
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new InternalInsuranceCompanyController(queryService, secretVerifier, new AuthContextResolver()))
            .build();

    @Test
    void internalEndpointWithValidSecretReturnsOk() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(queryService.existsActive(companyId)).thenReturn(true);

        mockMvc.perform(get("/internal/v1/insurance-companies/{companyId}/exists-active", companyId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "secret"))
                .andExpect(status().isOk());
    }

    @Test
    void internalEndpointWithInvalidSecretReturnsUnauthorized() throws Exception {
        UUID companyId = UUID.randomUUID();

        mockMvc.perform(get("/internal/v1/insurance-companies/{companyId}/exists-active", companyId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalCurrentAccessUsesGatewayIdentityHeaders() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(queryService.accessForCurrent(eq(companyId), any(AuthContext.class))).thenReturn(new AccessCheckResponse(
                userId,
                companyId,
                companyId,
                true,
                true,
                true,
                false
        ));

        mockMvc.perform(get("/internal/v1/insurance-companies/{companyId}/access/current", companyId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "secret")
                        .header(InternalAuthHeaders.USER_ID, userId.toString())
                        .header(InternalAuthHeaders.USER_ROLES, RoleConstants.INSURANCE_ADMIN)
                        .header(InternalAuthHeaders.CORRELATION_ID, "corr-1"))
                .andExpect(status().isOk());
    }
}
