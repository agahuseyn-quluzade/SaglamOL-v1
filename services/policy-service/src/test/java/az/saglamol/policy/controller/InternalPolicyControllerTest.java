package az.saglamol.policy.controller;

import az.saglamol.policy.service.EligibilityService;
import az.saglamol.policy.service.InternalServiceSecretVerifier;
import az.saglamol.policy.service.PolicyLimitService;
import az.saglamol.policy.service.PolicyService;
import az.saglamol.policy.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalPolicyController.class)
@Import({InternalServiceSecretVerifier.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "saglamol.security.internal-auth.secret=test-secret",
        "spring.cloud.config.enabled=false"
})
class InternalPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private EligibilityService eligibilityService;

    @MockBean
    private PolicyLimitService policyLimitService;

    @Test
    void internalActiveEndpointAcceptsValidSecret() throws Exception {
        UUID policyId = UUID.randomUUID();
        when(policyService.isPolicyActive(policyId)).thenReturn(true);

        mockMvc.perform(get("/internal/v1/policies/{policyId}/active", policyId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "test-secret"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(policyService).isPolicyActive(policyId);
    }

    @Test
    void internalActiveEndpointRejectsInvalidSecret() throws Exception {
        mockMvc.perform(get("/internal/v1/policies/{policyId}/active", UUID.randomUUID())
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "wrong-secret"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalActiveEndpointRejectsMissingSecret() throws Exception {
        mockMvc.perform(get("/internal/v1/policies/{policyId}/active", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
