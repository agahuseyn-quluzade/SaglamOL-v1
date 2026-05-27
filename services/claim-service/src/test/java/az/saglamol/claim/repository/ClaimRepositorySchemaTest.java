package az.saglamol.claim.repository;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class ClaimRepositorySchemaTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ClaimItemRepository itemRepository;

    @Autowired
    private ClaimDocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private ClaimDecisionRepository decisionRepository;

    @Autowired
    private ClaimStatusHistoryRepository statusHistoryRepository;

    @Test
    void repositoriesSaveAndFindClaimSchema() {
        UUID companyId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        Instant now = Instant.now();

        claimRepository.saveAndFlush(claim(claimId, "CLM-2001", companyId, patientId, hospitalId, now));
        itemRepository.saveAndFlush(new ClaimItem(
                UUID.randomUUID(),
                claimId,
                "Consultation",
                "CONS",
                new BigDecimal("100.00"),
                1,
                LocalDate.now(),
                UUID.randomUUID(),
                now,
                now
        ));
        documentReferenceRepository.saveAndFlush(new ClaimDocumentReference(
                UUID.randomUUID(),
                claimId,
                UUID.randomUUID(),
                "INVOICE",
                true,
                ClaimDocumentStatus.ATTACHED,
                now
        ));
        decisionRepository.saveAndFlush(new ClaimDecision(
                UUID.randomUUID(),
                claimId,
                ClaimDecisionType.APPROVED,
                UUID.randomUUID(),
                "approved",
                new BigDecimal("80.00"),
                now
        ));
        statusHistoryRepository.saveAndFlush(new ClaimStatusHistory(
                UUID.randomUUID(),
                claimId,
                ClaimStatus.SUBMITTED,
                ClaimStatus.UNDER_REVIEW,
                UUID.randomUUID(),
                "review started",
                now
        ));

        assertTrue(claimRepository.findByClaimNumber("CLM-2001").isPresent());
        assertEquals(1, claimRepository.findByInsuranceCompanyId(companyId).size());
        assertEquals(1, claimRepository.findByPatientProfileId(patientId).size());
        assertEquals(1, claimRepository.findByHospitalId(hospitalId).size());
        assertEquals(1, claimRepository.findByInsuranceCompanyIdAndPatientProfileId(companyId, patientId).size());
        assertEquals(1, claimRepository.findByInsuranceCompanyIdAndHospitalId(companyId, hospitalId).size());
        assertEquals(1, itemRepository.findByClaimId(claimId).size());
        assertEquals(1, documentReferenceRepository.findByClaimId(claimId).size());
        assertEquals(1, decisionRepository.findByClaimId(claimId).size());
        assertEquals(1, statusHistoryRepository.findByClaimId(claimId).size());
    }

    @Test
    void duplicateClaimNumberFails() {
        Instant now = Instant.now();

        claimRepository.saveAndFlush(
                claim(UUID.randomUUID(), "CLM-DUP", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), now)
        );

        assertThrows(DataIntegrityViolationException.class,
                () -> claimRepository.saveAndFlush(
                        claim(UUID.randomUUID(), "CLM-DUP", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), now)
                ));
    }

    private Claim claim(UUID claimId, String claimNumber, UUID companyId, UUID patientId, UUID hospitalId, Instant now) {
        return new Claim(
                claimId,
                claimNumber,
                UUID.randomUUID(),
                companyId,
                patientId,
                hospitalId,
                UUID.randomUUID(),
                ClaimStatus.SUBMITTED,
                "HOSPITAL",
                LocalDate.now(),
                new BigDecimal("100.00"),
                null,
                new BigDecimal("80.00"),
                new BigDecimal("20.00"),
                PayoutRecipientType.HOSPITAL,
                UUID.randomUUID(),
                "J10",
                null,
                "notes",
                15,
                "LOW",
                5,
                "LOW",
                true,
                now,
                now
        );
    }
}
