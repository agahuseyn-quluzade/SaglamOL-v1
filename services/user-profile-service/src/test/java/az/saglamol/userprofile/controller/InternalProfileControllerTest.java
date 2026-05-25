package az.saglamol.userprofile.controller;

import az.saglamol.userprofile.dto.response.UserProfileSummaryResponse;
import az.saglamol.userprofile.service.InternalProfileQueryService;
import az.saglamol.userprofile.service.InternalServiceSecretVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternalProfileControllerTest {

    private final InternalProfileQueryService queryService = mock(InternalProfileQueryService.class);
    private final InternalServiceSecretVerifier secretVerifier = new InternalServiceSecretVerifier("secret");
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new InternalProfileController(queryService, secretVerifier))
            .build();

    @Test
    void userSummaryWithValidSecretReturnsOk() throws Exception {
        UUID iamUserId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        when(queryService.summary(iamUserId)).thenReturn(new UserProfileSummaryResponse(
                iamUserId,
                patientProfileId,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                false,
                false,
                false
        ));

        mockMvc.perform(get("/internal/v1/profiles/users/{iamUserId}/summary", iamUserId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iamUserId").value(iamUserId.toString()))
                .andExpect(jsonPath("$.patientProfileId").value(patientProfileId.toString()))
                .andExpect(jsonPath("$.hasPatientProfile").value(true));
    }

    @Test
    void profileExistsWithInvalidSecretReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/internal/v1/profiles/patients/{patientProfileId}/exists", UUID.randomUUID())
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hospitalActiveWithValidSecretReturnsOk() throws Exception {
        UUID hospitalId = UUID.randomUUID();
        when(queryService.hospitalActive(hospitalId)).thenReturn(true);

        mockMvc.perform(get("/internal/v1/profiles/hospitals/{hospitalId}/exists-active", hospitalId)
                        .header(InternalServiceSecretVerifier.HEADER_NAME, "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
