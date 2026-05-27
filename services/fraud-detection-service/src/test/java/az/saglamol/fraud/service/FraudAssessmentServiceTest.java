package az.saglamol.fraud.service;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.fraud.client.ClaimDetailResponse;
import az.saglamol.fraud.client.ClaimInternalClient;
import az.saglamol.fraud.client.ClaimSummaryResponse;
import az.saglamol.fraud.client.HealthRecordInternalClient;
import az.saglamol.fraud.client.MedicalDocumentHashResponse;
import az.saglamol.fraud.client.MedicalDocumentSummaryResponse;
import az.saglamol.fraud.client.PolicyDetailResponse;
import az.saglamol.fraud.client.PolicyInternalClient;
import az.saglamol.fraud.client.ProfileScopeClient;
import az.saglamol.fraud.client.UserProfileSummaryResponse;
import az.saglamol.fraud.dto.FraudAssessmentDetailResponse;
import az.saglamol.fraud.entity.DocumentHashIndex;
import az.saglamol.fraud.entity.FraudAssessment;
import az.saglamol.fraud.entity.FraudAssessmentStatus;
import az.saglamol.fraud.entity.FraudLevel;
import az.saglamol.fraud.entity.FraudSignal;
import az.saglamol.fraud.entity.FraudSignalType;
import az.saglamol.fraud.entity.OutboxEvent;
import az.saglamol.fraud.mapper.FraudMapper;
import az.saglamol.fraud.mapper.FraudMapperImpl;
import az.saglamol.fraud.repository.DocumentHashIndexRepository;
import az.saglamol.fraud.repository.FraudAssessmentRepository;
import az.saglamol.fraud.repository.FraudSignalRepository;
import az.saglamol.fraud.rule.DoctorAnomalyRule;
import az.saglamol.fraud.rule.DuplicateDocumentRule;
import az.saglamol.fraud.rule.FrequentClaimsRule;
import az.saglamol.fraud.rule.HighAmountRule;
import az.saglamol.fraud.rule.HospitalAnomalyRule;
import az.saglamol.fraud.rule.SuspiciousTimingRule;
import az.saglamol.fraud.rule.WaitingPeriodRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FraudAssessmentServiceTest {

    private final FraudAssessmentRepository assessmentRepository = mock(FraudAssessmentRepository.class);
    private final FraudSignalRepository signalRepository = mock(FraudSignalRepository.class);
    private final DocumentHashIndexRepository hashIndexRepository = mock(DocumentHashIndexRepository.class);
    private final ClaimInternalClient claimClient = mock(ClaimInternalClient.class);
    private final HealthRecordInternalClient healthRecordClient = mock(HealthRecordInternalClient.class);
    private final PolicyInternalClient policyClient = mock(PolicyInternalClient.class);
    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final FraudMapper mapper = new FraudMapperImpl();
    @SuppressWarnings("unchecked")
    private final OutboxEventService<OutboxEvent> outboxEventService = mock(OutboxEventService.class);

    private FraudAssessmentService service;

    @BeforeEach
    void setUp() {
        FraudScoringService scoringService = new FraudScoringService(List.of(
                new DuplicateDocumentRule(),
                new FrequentClaimsRule(),
                new HighAmountRule(),
                new SuspiciousTimingRule(),
                new WaitingPeriodRule(),
                new HospitalAnomalyRule(),
                new DoctorAnomalyRule()
        ));
        FraudAccessService accessService = new FraudAccessService(profileScopeClient);
        service = new FraudAssessmentService(assessmentRepository, signalRepository, hashIndexRepository, claimClient,
                healthRecordClient, policyClient, scoringService, accessService, mapper, outboxEventService);
    }

    @Test
    void checkClaimRunsRulesPersistsSignalsAndPublishesOutbox() {
        UUID userId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        String hash = "a".repeat(64);

        ClaimDetailResponse claim = new ClaimDetailResponse(claimId, "CLM-1", policyId, companyId, patientId,
                hospitalId, doctorId, "SUBMITTED", "SURGERY", LocalDate.now(), new BigDecimal("1000.00"));
        when(claimClient.getClaim(claimId)).thenReturn(claim);
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, companyId, null));
        when(policyClient.getPolicy(policyId)).thenReturn(new PolicyDetailResponse(policyId, companyId, patientId,
                LocalDate.now().minusDays(3), LocalDate.now().plusYears(1), "ACTIVE"));
        when(healthRecordClient.documentsByClaim(claimId)).thenReturn(List.of(document(documentId, claimId, patientId, hospitalId)));
        when(healthRecordClient.documentHash(documentId)).thenReturn(new MedicalDocumentHashResponse(documentId, hash, patientId, claimId, hospitalId));
        when(hashIndexRepository.existsBySha256HashAndClaimIdNot(hash, claimId)).thenReturn(true);
        when(hashIndexRepository.findBySha256Hash(hash)).thenReturn(List.of());
        when(claimClient.claimsByCompany(eq(companyId), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                summaryClaim(UUID.randomUUID(), companyId, patientId, new BigDecimal("100.00")),
                summaryClaim(UUID.randomUUID(), companyId, patientId, new BigDecimal("120.00"))
        )));
        when(assessmentRepository.countByPatientProfileIdAndCreatedAtAfter(eq(patientId), any(Instant.class))).thenReturn(3L);
        when(assessmentRepository.countByHospitalIdAndFraudLevelInAndCreatedAtAfter(eq(hospitalId), any(), any(Instant.class))).thenReturn(3L);
        when(assessmentRepository.countByDoctorProfileIdAndFraudLevelInAndCreatedAtAfter(eq(doctorId), any(), any(Instant.class))).thenReturn(3L);
        when(assessmentRepository.save(any(FraudAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(signalRepository.save(any(FraudSignal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashIndexRepository.save(any(DocumentHashIndex.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FraudAssessmentDetailResponse response = service.checkClaim(auth(userId, RoleConstants.AGENT), claimId);

        assertEquals(FraudAssessmentStatus.COMPLETED, response.assessment().status());
        assertEquals(FraudLevel.CRITICAL, response.assessment().fraudLevel());
        assertTrue(response.assessment().manualReviewRequired());
        assertTrue(response.signals().stream().anyMatch(signal -> signal.signalType() == FraudSignalType.DUPLICATE_DOCUMENT));
        assertTrue(response.signals().stream().anyMatch(signal -> signal.signalType() == FraudSignalType.HIGH_AMOUNT));
        verify(hashIndexRepository).save(any(DocumentHashIndex.class));
        verify(outboxEventService).saveEvent(eq("FraudAssessment"), any(UUID.class), eq("FraudCheckCompletedEvent"), any(Object.class));
    }

    @Test
    void getByAssessmentRejectsOtherCompanyAgent() {
        UUID userId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        FraudAssessment assessment = assessment(assessmentId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, ownCompanyId, null));

        var exception = org.junit.jupiter.api.Assertions.assertThrows(
                az.saglamol.fraud.exception.FraudException.class,
                () -> service.getByAssessmentId(auth(userId, RoleConstants.AGENT), assessmentId)
        );

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    private AuthContext auth(UUID userId, String role) {
        return new AuthContext(userId, List.of(role), "corr", Map.of());
    }

    private UserProfileSummaryResponse summary(UUID userId, UUID companyId, UUID hospitalId) {
        return new UserProfileSummaryResponse(userId, null, null, UUID.randomUUID(), companyId,
                null, hospitalId, null, false, false, true, false, false);
    }

    private MedicalDocumentSummaryResponse document(UUID documentId, UUID claimId, UUID patientId, UUID hospitalId) {
        return new MedicalDocumentSummaryResponse(documentId, null, null, claimId, patientId, hospitalId,
                "INVOICE", "invoice.pdf", 1024L, "application/pdf", "CONFIRMED");
    }

    private ClaimSummaryResponse summaryClaim(UUID claimId, UUID companyId, UUID patientId, BigDecimal amount) {
        return new ClaimSummaryResponse(claimId, "CLM-S", UUID.randomUUID(), companyId, patientId, null,
                "PAID", "CONSULTATION", LocalDate.now().minusDays(10), amount, amount, BigDecimal.ZERO);
    }

    private FraudAssessment assessment(UUID id, UUID claimId, UUID companyId, UUID patientId, UUID hospitalId) {
        return new FraudAssessment(id, claimId, companyId, patientId, hospitalId, null,
                new BigDecimal("0.1000"), FraudLevel.LOW, false, FraudAssessmentStatus.COMPLETED,
                Instant.now(), Instant.now());
    }
}
