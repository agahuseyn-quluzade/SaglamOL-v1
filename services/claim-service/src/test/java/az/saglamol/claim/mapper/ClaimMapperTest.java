package az.saglamol.claim.mapper;

import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimDecision;
import az.saglamol.claim.entity.ClaimDecisionType;
import az.saglamol.claim.entity.ClaimDocumentReference;
import az.saglamol.claim.entity.ClaimDocumentStatus;
import az.saglamol.claim.entity.ClaimItem;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.ClaimStatusHistory;
import az.saglamol.claim.entity.PayoutRecipientType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimMapperTest {

    private final ClaimMapper mapper = Mappers.getMapper(ClaimMapper.class);

    @Test
    void mapsClaimToResponse() {
        UUID claimId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();

        Claim claim = new Claim(
                claimId,
                "CLM-1001",
                UUID.randomUUID(),
                companyId,
                patientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ClaimStatus.SUBMITTED,
                "HOSPITAL",
                LocalDate.of(2026, 5, 20),
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                new BigDecimal("800.00"),
                new BigDecimal("200.00"),
                PayoutRecipientType.HOSPITAL,
                UUID.randomUUID(),
                "A00",
                "covered",
                "initial review",
                25,
                "LOW",
                10,
                "LOW",
                true,
                now,
                now
        );

        var response = mapper.toResponse(claim);

        assertEquals(claimId, response.id());
        assertEquals("CLM-1001", response.claimNumber());
        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals(patientId, response.patientProfileId());
        assertEquals(ClaimStatus.SUBMITTED, response.status());
        assertEquals(PayoutRecipientType.HOSPITAL, response.payoutRecipientType());
    }

    @Test
    void mapsClaimChildEntitiesToResponses() {
        UUID claimId = UUID.randomUUID();
        Instant now = Instant.now();

        ClaimItem item = new ClaimItem(
                UUID.randomUUID(),
                claimId,
                "MRI scan",
                "MRI",
                new BigDecimal("250.00"),
                1,
                LocalDate.of(2026, 5, 21),
                UUID.randomUUID(),
                now,
                now
        );
        ClaimDocumentReference document = new ClaimDocumentReference(
                UUID.randomUUID(),
                claimId,
                UUID.randomUUID(),
                "INVOICE",
                true,
                ClaimDocumentStatus.ATTACHED,
                now
        );
        ClaimDecision decision = new ClaimDecision(
                UUID.randomUUID(),
                claimId,
                ClaimDecisionType.APPROVED,
                UUID.randomUUID(),
                "eligible",
                new BigDecimal("250.00"),
                now
        );
        ClaimStatusHistory history = new ClaimStatusHistory(
                UUID.randomUUID(),
                claimId,
                ClaimStatus.SUBMITTED,
                ClaimStatus.UNDER_REVIEW,
                UUID.randomUUID(),
                "assigned",
                now
        );

        assertEquals("MRI scan", mapper.toResponse(item).description());
        assertTrue(mapper.toResponse(document).required());
        assertEquals(ClaimDecisionType.APPROVED, mapper.toResponse(decision).decision());
        assertEquals(ClaimStatus.UNDER_REVIEW, mapper.toResponse(history).toStatus());
    }
}
