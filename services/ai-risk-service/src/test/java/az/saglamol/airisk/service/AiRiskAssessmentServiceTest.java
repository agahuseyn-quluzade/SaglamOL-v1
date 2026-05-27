package az.saglamol.airisk.service;

import az.saglamol.airisk.client.ClaimDetailResponse;
import az.saglamol.airisk.client.ClaimInternalClient;
import az.saglamol.airisk.client.FraudInternalClient;
import az.saglamol.airisk.client.FraudSummaryResponse;
import az.saglamol.airisk.client.PolicyDetailResponse;
import az.saglamol.airisk.client.PolicyInternalClient;
import az.saglamol.airisk.client.ProfileScopeClient;
import az.saglamol.airisk.client.UserProfileSummaryResponse;
import az.saglamol.airisk.config.AiRiskProperties;
import az.saglamol.airisk.dto.AiRiskAssessmentResponse;
import az.saglamol.airisk.entity.AiRequestLog;
import az.saglamol.airisk.entity.AiRiskAssessment;
import az.saglamol.airisk.entity.AiRiskAssessmentStatus;
import az.saglamol.airisk.entity.OutboxEvent;
import az.saglamol.airisk.entity.RiskLevel;
import az.saglamol.airisk.exception.AiRiskException;
import az.saglamol.airisk.mapper.AiRiskMapper;
import az.saglamol.airisk.mapper.AiRiskMapperImpl;
import az.saglamol.airisk.model.RiskModelResult;
import az.saglamol.airisk.repository.AiRequestLogRepository;
import az.saglamol.airisk.repository.AiRiskAssessmentRepository;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiRiskAssessmentServiceTest {

    private final AiRiskAssessmentRepository assessmentRepository = mock(AiRiskAssessmentRepository.class);
    private final AiRequestLogRepository requestLogRepository = mock(AiRequestLogRepository.class);
    private final ClaimInternalClient claimClient = mock(ClaimInternalClient.class);
    private final PolicyInternalClient policyClient = mock(PolicyInternalClient.class);
    private final FraudInternalClient fraudClient = mock(FraudInternalClient.class);
    private final ExternalAiRiskModelClient externalClient = mock(ExternalAiRiskModelClient.class);
    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final AiRiskMapper mapper = new AiRiskMapperImpl();
    @SuppressWarnings("unchecked")
    private final OutboxEventService<OutboxEvent> outboxEventService = mock(OutboxEventService.class);

    private AiRiskProperties properties;
    private AiRiskAssessmentService service;

    @BeforeEach
    void setUp() {
        properties = new AiRiskProperties();
        properties.setEnabled(true);
        properties.setModelName("test-model");
        AiPromptBuilder promptBuilder = new AiPromptBuilder();
        FallbackRiskModelClient fallbackClient = new FallbackRiskModelClient(properties);
        AiRiskAccessService accessService = new AiRiskAccessService(profileScopeClient);
        service = new AiRiskAssessmentService(assessmentRepository, requestLogRepository, claimClient, policyClient,
                fraudClient, promptBuilder, externalClient, fallbackClient, properties, accessService, mapper,
                outboxEventService);
    }

    @Test
    void successfulAiAssessmentCreatesAssessmentLogAndOutbox() {
        UUID userId = UUID.randomUUID();
        ClaimDetailResponse claim = claim();
        when(claimClient.getClaim(claim.id())).thenReturn(claim);
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, claim.insuranceCompanyId()));
        when(policyClient.getPolicy(claim.policyId())).thenReturn(policy(claim));
        when(fraudClient.companySummary(claim.insuranceCompanyId())).thenReturn(fraudSummary(claim.insuranceCompanyId()));
        when(externalClient.assess(any())).thenReturn(new RiskModelResult(
                new BigDecimal("0.7000"),
                RiskLevel.HIGH,
                new BigDecimal("0.8800"),
                List.of("External model detected high utilization risk"),
                "{\"ok\":true}",
                AiRiskAssessmentStatus.SUCCESS,
                "openai",
                "test-model",
                "{\"riskScore\":0.7}",
                null
        ));
        when(assessmentRepository.save(any(AiRiskAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestLogRepository.save(any(AiRequestLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiRiskAssessmentResponse response = service.assessClaim(auth(userId, RoleConstants.AGENT), claim.id());

        assertEquals(AiRiskAssessmentStatus.SUCCESS, response.status());
        assertEquals(RiskLevel.HIGH, response.riskLevel());
        assertEquals(new BigDecimal("0.7000"), response.riskScore());
        assertEquals(null, response.rawProviderResponse());
        verify(outboxEventService).saveEvent(eq("AiRiskAssessment"), any(UUID.class), eq("RiskAnalysisCompletedEvent"), any(Object.class));
    }

    @Test
    void aiDisabledUsesFallback() {
        properties.setEnabled(false);
        ClaimDetailResponse claim = claim();
        when(claimClient.getClaim(claim.id())).thenReturn(claim);
        when(profileScopeClient.userSummary(claim.patientProfileId())).thenReturn(summary(claim.patientProfileId(), claim.insuranceCompanyId()));
        when(assessmentRepository.save(any(AiRiskAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestLogRepository.save(any(AiRequestLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiRiskAssessmentResponse response = service.assessClaim(auth(claim.patientProfileId(), RoleConstants.INSURANCE_ADMIN), claim.id());

        assertEquals(AiRiskAssessmentStatus.FALLBACK_USED, response.status());
        assertTrue(response.reasons().stream().anyMatch(reason -> reason.contains("Fallback rules")));
    }

    @Test
    void providerFailureUsesFallbackAndLogsError() {
        UUID userId = UUID.randomUUID();
        ClaimDetailResponse claim = claim();
        when(claimClient.getClaim(claim.id())).thenReturn(claim);
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, claim.insuranceCompanyId()));
        when(externalClient.assess(any())).thenThrow(new IllegalStateException("timeout"));
        when(assessmentRepository.save(any(AiRiskAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestLogRepository.save(any(AiRequestLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AiRiskAssessmentResponse response = service.assessClaim(auth(userId, RoleConstants.AGENT), claim.id());

        ArgumentCaptor<AiRequestLog> logCaptor = ArgumentCaptor.forClass(AiRequestLog.class);
        verify(requestLogRepository).save(logCaptor.capture());
        assertEquals(AiRiskAssessmentStatus.FALLBACK_USED, response.status());
        assertEquals("timeout", logCaptor.getValue().getErrorMessage());
    }

    @Test
    void patientForbidden() {
        UUID userId = UUID.randomUUID();
        ClaimDetailResponse claim = claim();
        when(claimClient.getClaim(claim.id())).thenReturn(claim);

        AiRiskException exception = assertThrows(AiRiskException.class,
                () -> service.assessClaim(auth(userId, RoleConstants.PATIENT), claim.id()));

        assertEquals("PATIENT_FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void agentOtherCompanyForbidden() {
        UUID userId = UUID.randomUUID();
        ClaimDetailResponse claim = claim();
        when(claimClient.getClaim(claim.id())).thenReturn(claim);
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, UUID.randomUUID()));

        AiRiskException exception = assertThrows(AiRiskException.class,
                () -> service.assessClaim(auth(userId, RoleConstants.AGENT), claim.id()));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void promptPayloadDoesNotIncludeClaimNumberOrPatientProfileId() throws Exception {
        ClaimDetailResponse claim = claim();
        String json = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
                .writeValueAsString(new AiPromptBuilder().build(claim, policy(claim), fraudSummary(claim.insuranceCompanyId())));

        assertFalse(json.contains("CLM-PII-001"));
        assertFalse(json.contains(claim.patientProfileId().toString()));
        assertTrue(json.contains(claim.id().toString()));
    }

    private AuthContext auth(UUID userId, String role) {
        return new AuthContext(userId, List.of(role), "corr", Map.of());
    }

    private ClaimDetailResponse claim() {
        return new ClaimDetailResponse(UUID.randomUUID(), "CLM-PII-001", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "SUBMITTED", "SURGERY",
                LocalDate.now(), new BigDecimal("1500.00"));
    }

    private PolicyDetailResponse policy(ClaimDetailResponse claim) {
        return new PolicyDetailResponse(claim.policyId(), claim.insuranceCompanyId(), claim.patientProfileId(),
                LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10), "ACTIVE",
                new BigDecimal("5000.00"), new BigDecimal("1000.00"), BigDecimal.ZERO);
    }

    private FraudSummaryResponse fraudSummary(UUID companyId) {
        return new FraudSummaryResponse(companyId, 10, 8, 1, 1, new BigDecimal("0.2000"));
    }

    private UserProfileSummaryResponse summary(UUID userId, UUID companyId) {
        return new UserProfileSummaryResponse(userId, null, null, UUID.randomUUID(), companyId,
                null, null, null, false, false, true, false, false);
    }
}
