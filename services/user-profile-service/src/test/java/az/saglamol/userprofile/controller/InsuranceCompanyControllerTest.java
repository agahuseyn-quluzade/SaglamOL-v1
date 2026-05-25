package az.saglamol.userprofile.controller;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.userprofile.dto.request.CreateInsuranceCompanyRequest;
import az.saglamol.userprofile.dto.response.InsuranceCompanyResponse;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.service.InsuranceCompanyService;
import az.saglamol.userprofile.service.InsuranceCompanyStaffService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InsuranceCompanyControllerTest {

    private final InsuranceCompanyService companyService = mock(InsuranceCompanyService.class);
    private final InsuranceCompanyStaffService staffService = mock(InsuranceCompanyStaffService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new InsuranceCompanyController(companyService, staffService))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void clearContext() {
        AuthContextHolder.clear();
    }

    @Test
    void adminCreatesInsuranceCompanyReturnsCreated() throws Exception {
        AuthContextHolder.set(new AuthContext(UUID.randomUUID(), List.of(RoleConstants.ADMIN), "corr-1", Map.of()));
        when(companyService.createCompany(any(), any())).thenReturn(new InsuranceCompanyResponse(
                UUID.randomUUID(),
                "Company",
                "TAX-1",
                "LIC-1",
                "company@saglamol.az",
                "+994501234567",
                InsuranceCompanyStatus.PENDING,
                Instant.now()
        ));

        mockMvc.perform(post("/api/v1/insurance-companies")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CreateInsuranceCompanyRequest(
                                        "Company",
                                        "TAX-1",
                                        "LIC-1",
                                        "company@saglamol.az",
                                        "+994501234567",
                                        null
                                )
                        )))
                .andExpect(status().isCreated());
    }
}
