package az.saglamol.claim.service;

import az.saglamol.claim.client.PolicyInternalClient;
import az.saglamol.claim.client.ProfileInternalClient;
import az.saglamol.claim.client.dto.EligibilityCheckResponse;
import az.saglamol.claim.client.dto.PolicyDetailResponse;
import az.saglamol.claim.client.dto.PolicyLimitReservationResponse;
import az.saglamol.claim.client.dto.UserProfileSummaryResponse;
import az.saglamol.claim.dto.request.ClaimItemRequest;
import az.saglamol.claim.dto.request.CreateClaimRequest;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDocumentReference;
import az.saglamol.claim.entity.ClaimDocumentStatus;
import az.saglamol.claim.entity.ClaimItem;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.entity.PayoutRecipientType;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.claim.mapper.ClaimMapper;
import az.saglamol.claim.repository.ClaimDocumentReferenceRepository;
import az.saglamol.claim.repository.ClaimItemRepository;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.ClaimStatusHistoryRepository;
import az.saglamol.common.events.claim.ClaimSubmittedEvent;
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
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimServiceLifecycleTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimItemRepository itemRepository;
    @Mock
    private ClaimDocumentReferenceRepository documentRepository;
    @Mock
    private ClaimStatusHistoryRepository historyRepository;
    @Mock
    private PolicyInternalClient policyClient;
    @Mock
    private ProfileInternalClient profileClient;
    @Mock
    private OutboxEventService<OutboxEvent> outboxEventService;

    private ClaimService claimService;
    private UUID userId;
    private UUID patientId;
    private UUID companyId;
    private UUID hospitalId;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        ClaimMapper mapper = Mappers.getMapper(ClaimMapper.class);
        ClaimAccessService accessService = new ClaimAccessService(profileClient);
        claimService = new ClaimService(
                claimRepository,
                itemRepository,
                documentRepository,
                historyRepository,
                policyClient,
                accessService,
                mapper,
                outboxEventService
        );
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();
        policyId = UUID.randomUUID();
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void patientCanCreateDraftClaimForOwnPolicy() {
        when(claimRepository.existsByClaimNumber("CLM-1")).thenReturn(false);
        when(policyClient.getPolicy(policyId)).thenReturn(policy());
        when(profileClient.userSummary(userId)).thenReturn(patientSummary());

        var response = claimService.createClaim(patientAuth(), createRequest(null));

        assertEquals(ClaimStatus.DRAFT, response.status());
        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals(patientId, response.patientProfileId());
    }

    @Test
    void hospitalStaffCanCreateClaimForOwnHospital() {
        when(claimRepository.existsByClaimNumber("CLM-1")).thenReturn(false);
        when(policyClient.getPolicy(policyId)).thenReturn(policy());
        when(profileClient.userSummary(userId)).thenReturn(hospitalSummary());

        var response = claimService.createClaim(hospitalAuth(), createRequest(hospitalId));

        assertEquals(hospitalId, response.hospitalId());
        assertEquals(ClaimStatus.DRAFT, response.status());
    }

    @Test
    void submitClaimCalculatesAmountReservesLimitAndWritesOutboxEvent() {
        UUID claimId = UUID.randomUUID();
        UUID itemDocumentId = UUID.randomUUID();
        UUID attachedDocumentId = UUID.randomUUID();
        Claim claim = draftClaim(claimId);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(profileClient.userSummary(userId)).thenReturn(patientSummary());
        when(itemRepository.findByClaimId(claimId)).thenReturn(List.of(
                item(claimId, "100.00", 2, itemDocumentId),
                item(claimId, "50.00", 1)
        ));
        when(policyClient.getPolicy(policyId)).thenReturn(policy());
        when(policyClient.checkEligibility(any())).thenReturn(new EligibilityCheckResponse(
                true,
                policyId,
                companyId,
                patientId,
                null,
                "HOSPITAL",
                new BigDecimal("250.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1000.00"),
                80,
                true,
                List.of()
        ));
        UUID reservationId = UUID.randomUUID();
        when(policyClient.reserveLimit(eq(policyId), any())).thenReturn(new PolicyLimitReservationResponse(
                reservationId,
                policyId,
                companyId,
                claimId,
                new BigDecimal("200.00"),
                "RESERVED",
                Instant.now(),
                Instant.now()
        ));
        when(documentRepository.findByClaimId(claimId)).thenReturn(List.of(new ClaimDocumentReference(
                UUID.randomUUID(),
                claimId,
                attachedDocumentId,
                "INVOICE",
                true,
                ClaimDocumentStatus.ATTACHED,
                Instant.now()
        )));

        var response = claimService.submitClaim(patientAuth(), claimId);

        assertEquals(ClaimStatus.SUBMITTED, response.status());
        assertEquals(new BigDecimal("250.00"), response.claimAmount());
        assertEquals(new BigDecimal("200.00"), response.coveredAmount());
        assertEquals(new BigDecimal("50.00"), response.patientPayAmount());
        assertEquals(reservationId, response.policyReservationId());
        verify(historyRepository).save(any());
        var eventCaptor = forClass(Object.class);
        verify(outboxEventService).saveEvent(eq("Claim"), eq(claimId), eq(ClaimSubmittedEvent.class.getSimpleName()), eventCaptor.capture());
        ClaimSubmittedEvent event = (ClaimSubmittedEvent) eventCaptor.getValue();
        assertEquals(List.of(itemDocumentId, attachedDocumentId), event.documentIds());
    }

    @Test
    void submitWithoutItemFails() {
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(draftClaim(claimId)));
        when(profileClient.userSummary(userId)).thenReturn(patientSummary());
        when(itemRepository.findByClaimId(claimId)).thenReturn(List.of());

        ClaimException exception = assertThrows(ClaimException.class, () -> claimService.submitClaim(patientAuth(), claimId));

        assertEquals("CLAIM_ITEM_REQUIRED", exception.getErrorCode());
        verify(policyClient, never()).checkEligibility(any());
    }

    @Test
    void submitNotEligibleFailsWithoutReservation() {
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(draftClaim(claimId)));
        when(profileClient.userSummary(userId)).thenReturn(patientSummary());
        when(itemRepository.findByClaimId(claimId)).thenReturn(List.of(item(claimId, "100.00", 1)));
        when(policyClient.getPolicy(policyId)).thenReturn(policy());
        when(policyClient.checkEligibility(any())).thenReturn(new EligibilityCheckResponse(
                false,
                policyId,
                companyId,
                patientId,
                null,
                "HOSPITAL",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                false,
                List.of("Waiting period")
        ));

        ClaimException exception = assertThrows(ClaimException.class, () -> claimService.submitClaim(patientAuth(), claimId));

        assertEquals("CLAIM_NOT_ELIGIBLE", exception.getErrorCode());
        verify(policyClient, never()).reserveLimit(any(), any());
        verify(outboxEventService, never()).saveEvent(any(), any(), any(), any(Object.class));
    }

    private AuthContext patientAuth() {
        return new AuthContext(userId, List.of(RoleConstants.PATIENT), UUID.randomUUID().toString(), Map.of());
    }

    private AuthContext hospitalAuth() {
        return new AuthContext(userId, List.of(RoleConstants.HOSPITAL_STAFF), UUID.randomUUID().toString(), Map.of());
    }

    private CreateClaimRequest createRequest(UUID requestHospitalId) {
        return new CreateClaimRequest(
                "CLM-1",
                policyId,
                companyId,
                patientId,
                requestHospitalId,
                UUID.randomUUID(),
                "HOSPITAL",
                LocalDate.now(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PayoutRecipientType.PATIENT,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Claim draftClaim(UUID claimId) {
        return new Claim(
                claimId,
                "CLM-" + claimId,
                policyId,
                companyId,
                patientId,
                null,
                null,
                ClaimStatus.DRAFT,
                "HOSPITAL",
                LocalDate.now(),
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                PayoutRecipientType.PATIENT,
                null,
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

    private ClaimItem item(UUID claimId, String amount, int quantity) {
        return item(claimId, amount, quantity, null);
    }

    private ClaimItem item(UUID claimId, String amount, int quantity, UUID documentId) {
        return new ClaimItem(UUID.randomUUID(), claimId, "Service", null, new BigDecimal(amount),
                quantity, LocalDate.now(), documentId, Instant.now(), Instant.now());
    }

    private PolicyDetailResponse policy() {
        return new PolicyDetailResponse(policyId, "POL-1", companyId, UUID.randomUUID(), patientId, null,
                "ACTIVE", LocalDate.now(), LocalDate.now().plusYears(1), BigDecimal.ZERO,
                new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, Instant.now(), Instant.now(), 0L);
    }

    private UserProfileSummaryResponse patientSummary() {
        return new UserProfileSummaryResponse(userId, patientId, null, null, null, null, null, null,
                true, false, false, false, false);
    }

    private UserProfileSummaryResponse hospitalSummary() {
        return new UserProfileSummaryResponse(userId, null, null, null, null, UUID.randomUUID(), hospitalId, null,
                false, false, false, true, false);
    }
}
