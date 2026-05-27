package az.saglamol.claim.service;

import az.saglamol.claim.client.PolicyInternalClient;
import az.saglamol.claim.client.ProfileInternalClient;
import az.saglamol.claim.client.dto.UserProfileSummaryResponse;
import az.saglamol.claim.dto.request.ReviewClaimRequest;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDecision;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.entity.PayoutRecipientType;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.mapper.ClaimMapper;
import az.saglamol.claim.repository.ClaimDecisionRepository;
import az.saglamol.claim.repository.ClaimDocumentReferenceRepository;
import az.saglamol.claim.repository.ClaimItemRepository;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.events.claim.ClaimRejectedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimReviewServiceLifecycleTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimItemRepository itemRepository;
    @Mock
    private ClaimDocumentReferenceRepository documentRepository;
    @Mock
    private ClaimStatusHistoryRepository historyRepository;
    @Mock
    private ClaimDecisionRepository decisionRepository;
    @Mock
    private PolicyInternalClient policyClient;
    @Mock
    private ProfileInternalClient profileClient;
    @Mock
    private OutboxEventService<OutboxEvent> outboxEventService;

    private ClaimReviewService reviewService;
    private UUID userId;
    private UUID companyId;
    private UUID patientId;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        ClaimMapper mapper = Mappers.getMapper(ClaimMapper.class);
        ClaimAccessService accessService = new ClaimAccessService(profileClient);
        ClaimService claimService = new ClaimService(
                claimRepository,
                itemRepository,
                documentRepository,
                historyRepository,
                policyClient,
                accessService,
                mapper,
                outboxEventService
        );
        reviewService = new ClaimReviewService(
                claimService,
                claimRepository,
                decisionRepository,
                historyRepository,
                policyClient,
                accessService,
                mapper,
                outboxEventService
        );
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        policyId = UUID.randomUUID();
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisionRepository.save(any(ClaimDecision.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void agentCanStartReviewForOwnCompanyClaim() {
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim(claimId, ClaimStatus.SUBMITTED)));
        when(profileClient.userSummary(userId)).thenReturn(companySummary(companyId));

        var response = reviewService.startReview(agentAuth(), claimId);

        assertEquals(ClaimStatus.UNDER_REVIEW, response.status());
        verify(historyRepository).save(any());
    }

    @Test
    void agentCannotReviewOtherCompanyClaim() {
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim(claimId, ClaimStatus.SUBMITTED)));
        when(profileClient.userSummary(userId)).thenReturn(companySummary(UUID.randomUUID()));

        ClaimException exception = assertThrows(ClaimException.class, () -> reviewService.startReview(agentAuth(), claimId));

        assertEquals("FORBIDDEN", exception.getErrorCode());
    }

    @Test
    void approveCommitsReservationWritesDecisionAndOutboxEvent() {
        UUID claimId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        Claim claim = claim(claimId, ClaimStatus.UNDER_REVIEW, reservationId);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(profileClient.userSummary(userId)).thenReturn(companySummary(companyId));

        var response = reviewService.approveClaim(agentAuth(), claimId,
                new ReviewClaimRequest(new BigDecimal("80.00"), "valid"));

        assertEquals(ClaimStatus.APPROVED, response.status());
        assertEquals(new BigDecimal("80.00"), response.approvedAmount());
        verify(policyClient).confirmReservation(reservationId);
        verify(decisionRepository).save(any(ClaimDecision.class));
        verify(outboxEventService).saveEvent(eq("Claim"), eq(claimId), eq(ClaimApprovedEvent.class.getSimpleName()), any(Object.class));
    }

    @Test
    void rejectRequiresReasonAndReleasesReservation() {
        UUID claimId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        Claim claim = claim(claimId, ClaimStatus.UNDER_REVIEW, reservationId);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(profileClient.userSummary(userId)).thenReturn(companySummary(companyId));

        var response = reviewService.rejectClaim(agentAuth(), claimId, new ReviewClaimRequest(null, "not covered"));

        assertEquals(ClaimStatus.REJECTED, response.status());
        verify(policyClient).releaseReservation(eq(reservationId), any());
        verify(decisionRepository).save(any(ClaimDecision.class));
        verify(outboxEventService).saveEvent(eq("Claim"), eq(claimId), eq(ClaimRejectedEvent.class.getSimpleName()), any(Object.class));
    }

    @Test
    void rejectWithoutReasonFails() {
        UUID claimId = UUID.randomUUID();

        ClaimException exception = assertThrows(ClaimException.class,
                () -> reviewService.rejectClaim(agentAuth(), claimId, new ReviewClaimRequest(null, " ")));

        assertEquals("REASON_REQUIRED", exception.getErrorCode());
        verify(claimRepository, never()).findById(any());
    }

    @Test
    void invalidTransitionFails() {
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim(claimId, ClaimStatus.DRAFT)));
        when(profileClient.userSummary(userId)).thenReturn(companySummary(companyId));

        ClaimException exception = assertThrows(ClaimException.class, () -> reviewService.startReview(agentAuth(), claimId));

        assertEquals("INVALID_CLAIM_STATUS", exception.getErrorCode());
    }

    private AuthContext agentAuth() {
        return new AuthContext(userId, List.of(RoleConstants.AGENT), UUID.randomUUID().toString(), Map.of());
    }

    private UserProfileSummaryResponse companySummary(UUID responseCompanyId) {
        return new UserProfileSummaryResponse(userId, null, null, UUID.randomUUID(), responseCompanyId,
                null, null, null, false, false, true, false, false);
    }

    private Claim claim(UUID claimId, ClaimStatus status) {
        return claim(claimId, status, null);
    }

    private Claim claim(UUID claimId, ClaimStatus status, UUID reservationId) {
        return new Claim(
                claimId,
                "CLM-" + claimId,
                policyId,
                companyId,
                patientId,
                UUID.randomUUID(),
                null,
                status,
                "HOSPITAL",
                LocalDate.now(),
                new BigDecimal("100.00"),
                null,
                new BigDecimal("80.00"),
                new BigDecimal("20.00"),
                PayoutRecipientType.HOSPITAL,
                reservationId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );
    }
}
