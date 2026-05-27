package az.saglamol.claim.service;

import az.saglamol.claim.client.PolicyInternalClient;
import az.saglamol.claim.client.dto.EligibilityCheckRequest;
import az.saglamol.claim.client.dto.LimitReservationRequest;
import az.saglamol.claim.dto.request.AttachClaimDocumentRequest;
import az.saglamol.claim.dto.request.ClaimItemRequest;
import az.saglamol.claim.dto.request.ClaimSearchFilters;
import az.saglamol.claim.dto.request.CreateClaimRequest;
import az.saglamol.claim.dto.response.ClaimItemResponse;
import az.saglamol.claim.dto.response.ClaimResponse;
import az.saglamol.claim.dto.response.ClaimSummaryResponse;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDocumentReference;
import az.saglamol.claim.entity.ClaimDocumentStatus;
import az.saglamol.claim.entity.ClaimItem;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.ClaimStatusHistory;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimItemRepository itemRepository;
    private final ClaimDocumentReferenceRepository documentRepository;
    private final ClaimStatusHistoryRepository statusHistoryRepository;
    private final PolicyInternalClient policyClient;
    private final ClaimAccessService accessService;
    private final ClaimMapper mapper;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public ClaimService(
            ClaimRepository claimRepository,
            ClaimItemRepository itemRepository,
            ClaimDocumentReferenceRepository documentRepository,
            ClaimStatusHistoryRepository statusHistoryRepository,
            PolicyInternalClient policyClient,
            ClaimAccessService accessService,
            ClaimMapper mapper,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.documentRepository = documentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.policyClient = policyClient;
        this.accessService = accessService;
        this.mapper = mapper;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public ClaimResponse createClaim(AuthContext authContext, CreateClaimRequest request) {
        if (claimRepository.existsByClaimNumber(request.claimNumber())) {
            throw new ClaimException("CLAIM_ALREADY_EXISTS", "Claim number already exists");
        }
        var policy = policyClient.getPolicy(request.policyId());
        accessService.requireCanCreateClaim(authContext, policy, request.patientProfileId(), request.hospitalId());
        if (!policy.patientProfileId().equals(request.patientProfileId())) {
            throw new ClaimException("POLICY_PATIENT_MISMATCH", "Claim patient does not match policy patient");
        }
        Instant now = Instant.now();
        Claim claim = new Claim(
                UUID.randomUUID(),
                request.claimNumber(),
                request.policyId(),
                policy.insuranceCompanyId(),
                policy.patientProfileId(),
                request.hospitalId(),
                request.doctorProfileId(),
                ClaimStatus.DRAFT,
                request.serviceType(),
                request.treatmentDate(),
                request.claimAmount(),
                null,
                request.coveredAmount(),
                request.patientPayAmount(),
                request.payoutRecipientType(),
                request.policyReservationId(),
                request.diagnosisCode(),
                request.reason(),
                request.notes(),
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
        Claim saved = claimRepository.save(claim);
        if (request.items() != null) {
            request.items().forEach(item -> itemRepository.save(new ClaimItem(
                    UUID.randomUUID(),
                    saved.getId(),
                    item.description(),
                    item.serviceCode(),
                    item.amount(),
                    item.quantity(),
                    item.serviceDate(),
                    item.documentId(),
                    now,
                    now
            )));
        }
        if (request.documents() != null) {
            request.documents().forEach(document -> documentRepository.save(new ClaimDocumentReference(
                    UUID.randomUUID(),
                    saved.getId(),
                    document.documentId(),
                    document.documentType(),
                    document.required(),
                    document.status(),
                    now
            )));
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    public ClaimItemResponse addClaimItem(AuthContext authContext, UUID claimId, ClaimItemRequest request) {
        Claim claim = requireClaim(claimId);
        accessService.requireCanMutateDraft(authContext, claim);
        requireStatus(claim, ClaimStatus.DRAFT);
        Instant now = Instant.now();
        ClaimItem item = itemRepository.save(new ClaimItem(
                UUID.randomUUID(),
                claimId,
                request.description(),
                request.serviceCode(),
                request.amount(),
                request.quantity(),
                request.serviceDate(),
                request.documentId(),
                now,
                now
        ));
        return mapper.toResponse(item);
    }

    @Transactional
    public void attachDocument(AuthContext authContext, UUID claimId, AttachClaimDocumentRequest request) {
        Claim claim = requireClaim(claimId);
        accessService.requireCanMutateDraft(authContext, claim);
        documentRepository.save(new ClaimDocumentReference(
                UUID.randomUUID(),
                claimId,
                request.documentId(),
                request.documentType(),
                request.required(),
                request.status() == null ? ClaimDocumentStatus.ATTACHED : request.status(),
                Instant.now()
        ));
    }

    @Transactional
    public ClaimResponse submitClaim(AuthContext authContext, UUID claimId) {
        Claim claim = requireClaim(claimId);
        accessService.requireCanMutateDraft(authContext, claim);
        requireStatus(claim, ClaimStatus.DRAFT);
        List<ClaimItem> items = itemRepository.findByClaimId(claimId);
        if (items.isEmpty()) {
            throw new ClaimException("CLAIM_ITEM_REQUIRED", "At least one claim item is required");
        }
        BigDecimal calculatedAmount = items.stream()
                .map(item -> item.getAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var policy = policyClient.getPolicy(claim.getPolicyId());
        if (!policy.patientProfileId().equals(claim.getPatientProfileId())) {
            throw new ClaimException("POLICY_PATIENT_MISMATCH", "Claim patient does not match policy patient");
        }

        var eligibility = policyClient.checkEligibility(new EligibilityCheckRequest(
                policy.id(),
                policy.insuranceCompanyId(),
                policy.patientProfileId(),
                claim.getHospitalId(),
                claim.getServiceType(),
                calculatedAmount,
                claim.getTreatmentDate()
        ));
        if (!eligibility.eligible()) {
            throw new ClaimException("CLAIM_NOT_ELIGIBLE", "Claim is not eligible for submission");
        }
        BigDecimal coveredAmount = eligibility.coveredAmount();
        BigDecimal patientPayAmount = calculatedAmount.subtract(coveredAmount);
        PayoutRecipientType recipientType = claim.getHospitalId() == null
                ? PayoutRecipientType.PATIENT
                : PayoutRecipientType.HOSPITAL;
        var reservation = policyClient.reserveLimit(policy.id(), new LimitReservationRequest(
                claim.getId(),
                policy.insuranceCompanyId(),
                coveredAmount
        ));

        ClaimStatus fromStatus = claim.getStatus();
        Instant now = Instant.now();
        claim.submit(calculatedAmount, coveredAmount, patientPayAmount, recipientType, reservation.id(), now);
        Claim saved = claimRepository.save(claim);
        statusHistoryRepository.save(new ClaimStatusHistory(
                UUID.randomUUID(),
                claim.getId(),
                fromStatus,
                ClaimStatus.SUBMITTED,
                authContext.userId(),
                "Claim submitted",
                now
        ));
        outboxEventService.saveEvent("Claim", claim.getId(), ClaimSubmittedEvent.class.getSimpleName(), new ClaimSubmittedEvent(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getPolicyId(),
                claim.getInsuranceCompanyId(),
                claim.getPatientProfileId(),
                claim.getHospitalId(),
                claim.getDoctorProfileId(),
                claim.getServiceType(),
                claim.getClaimAmount(),
                documentRepository.findByClaimId(claim.getId()).stream().map(ClaimDocumentReference::getDocumentId).toList(),
                now
        ));
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(AuthContext authContext, UUID claimId) {
        Claim claim = requireClaim(claimId);
        accessService.requireCanViewClaim(authContext, claim);
        return mapper.toResponse(claim);
    }

    @Transactional(readOnly = true)
    public Page<ClaimSummaryResponse> getMyClaims(AuthContext authContext, Pageable pageable) {
        UUID patientProfileId = accessService.resolveMyPatientProfileId(authContext);
        return claimRepository.findByPatientProfileId(patientProfileId, pageable).map(mapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimSummaryResponse> searchClaims(AuthContext authContext, ClaimSearchFilters filters, Pageable pageable) {
        ClaimSearchFilters effectiveFilters = filters == null
                ? new ClaimSearchFilters(null, null, null, null, null, null)
                : filters;
        var scope = accessService.resolveSearchScope(
                authContext,
                effectiveFilters.companyId(),
                effectiveFilters.patientProfileId(),
                effectiveFilters.hospitalId()
        );
        return claimRepository.search(
                scope.companyId(),
                scope.patientProfileId(),
                scope.hospitalId(),
                effectiveFilters.status(),
                effectiveFilters.fromDate(),
                effectiveFilters.toDate(),
                pageable
        ).map(mapper::toSummaryResponse);
    }

    Claim requireClaim(UUID claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimException("CLAIM_NOT_FOUND", "Claim was not found"));
    }

    void requireStatus(Claim claim, ClaimStatus requiredStatus) {
        if (claim.getStatus() != requiredStatus) {
            throw new ClaimException("INVALID_CLAIM_STATUS", "Claim must be in " + requiredStatus + " status");
        }
    }
}
