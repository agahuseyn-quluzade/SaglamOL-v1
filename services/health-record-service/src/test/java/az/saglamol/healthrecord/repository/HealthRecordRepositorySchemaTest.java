package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.DocumentHashIndex;
import az.saglamol.healthrecord.entity.DocumentType;
import az.saglamol.healthrecord.entity.HealthAccessLog;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.HealthRecordStatus;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;
import az.saglamol.healthrecord.entity.RecordType;
import az.saglamol.healthrecord.entity.Treatment;
import az.saglamol.healthrecord.entity.VisitType;
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
class HealthRecordRepositorySchemaTest {

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private MedicalDocumentRepository documentRepository;

    @Autowired
    private DocumentHashIndexRepository hashIndexRepository;

    @Autowired
    private HealthAccessLogRepository accessLogRepository;

    @Test
    void repositoriesSaveAndFindHealthRecordSchema() {
        UUID patientId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        Instant now = Instant.now();

        healthRecordRepository.saveAndFlush(record(recordId, patientId, hospitalId, claimId, now));
        Treatment treatment = treatmentRepository.saveAndFlush(new Treatment(
                UUID.randomUUID(),
                recordId,
                "HOSPITAL",
                "SURGERY",
                "Appendectomy",
                LocalDate.now(),
                null,
                "Antibiotics",
                new BigDecimal("1000.00"),
                new BigDecimal("950.00"),
                now,
                now
        ));
        MedicalDocument document = documentRepository.saveAndFlush(document(
                UUID.randomUUID(),
                recordId,
                treatment.getId(),
                claimId,
                patientId,
                hospitalId,
                "b".repeat(64),
                now
        ));
        hashIndexRepository.saveAndFlush(new DocumentHashIndex(
                UUID.randomUUID(),
                document.getSha256Hash(),
                document.getId(),
                patientId,
                claimId,
                hospitalId,
                now
        ));
        accessLogRepository.saveAndFlush(new HealthAccessLog(
                UUID.randomUUID(),
                recordId,
                document.getId(),
                UUID.randomUUID(),
                "HOSPITAL_STAFF",
                "Claim review",
                now
        ));

        assertEquals(1, healthRecordRepository.findByPatientProfileId(patientId).size());
        assertEquals(1, healthRecordRepository.findByHospitalId(hospitalId).size());
        assertEquals(1, healthRecordRepository.findByClaimId(claimId).size());
        assertEquals(1, treatmentRepository.findByHealthRecordId(recordId).size());
        assertEquals(1, documentRepository.findByClaimId(claimId).size());
        assertEquals(1, documentRepository.findByPatientProfileId(patientId).size());
        assertEquals(1, documentRepository.findByHospitalId(hospitalId).size());
        assertTrue(hashIndexRepository.findBySha256Hash(document.getSha256Hash()).isPresent());
        assertEquals(1, accessLogRepository.findByDocumentId(document.getId()).size());
    }

    @Test
    void duplicateHashIndexFails() {
        UUID patientId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        String hash = "c".repeat(64);
        Instant now = Instant.now();

        healthRecordRepository.saveAndFlush(record(recordId, patientId, hospitalId, claimId, now));
        MedicalDocument document = documentRepository.saveAndFlush(document(
                UUID.randomUUID(),
                recordId,
                null,
                claimId,
                patientId,
                hospitalId,
                hash,
                now
        ));
        hashIndexRepository.saveAndFlush(new DocumentHashIndex(UUID.randomUUID(), hash, document.getId(), patientId, claimId, hospitalId, now));

        assertThrows(DataIntegrityViolationException.class, () -> hashIndexRepository.saveAndFlush(
                new DocumentHashIndex(UUID.randomUUID(), hash, document.getId(), patientId, claimId, hospitalId, now)
        ));
    }

    private HealthRecord record(UUID recordId, UUID patientId, UUID hospitalId, UUID claimId, Instant now) {
        return new HealthRecord(
                recordId,
                patientId,
                UUID.randomUUID(),
                hospitalId,
                UUID.randomUUID(),
                claimId,
                LocalDate.now(),
                VisitType.INPATIENT,
                RecordType.PROCEDURE,
                "Appendicitis",
                "Stable",
                HealthRecordStatus.ACTIVE,
                now,
                now
        );
    }

    private MedicalDocument document(UUID documentId, UUID recordId, UUID treatmentId, UUID claimId,
                                     UUID patientId, UUID hospitalId, String hash, Instant now) {
        return new MedicalDocument(
                documentId,
                recordId,
                treatmentId,
                claimId,
                patientId,
                hospitalId,
                UUID.randomUUID(),
                DocumentType.INVOICE,
                "invoice.pdf",
                1024L,
                "application/pdf",
                "medical-documents",
                "claims/invoice.pdf",
                hash,
                MedicalDocumentStatus.CONFIRMED,
                now,
                now
        );
    }
}
