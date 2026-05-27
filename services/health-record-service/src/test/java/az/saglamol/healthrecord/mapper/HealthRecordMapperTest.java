package az.saglamol.healthrecord.mapper;

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
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthRecordMapperTest {

    private final HealthRecordMapper mapper = Mappers.getMapper(HealthRecordMapper.class);

    @Test
    void mapsHealthRecordToResponse() {
        UUID recordId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        Instant now = Instant.now();
        HealthRecord record = new HealthRecord(
                recordId,
                patientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 5, 26),
                VisitType.OUTPATIENT,
                RecordType.CONSULTATION,
                "Flu",
                "Rest",
                HealthRecordStatus.ACTIVE,
                now,
                now
        );

        var response = mapper.toResponse(record);

        assertEquals(recordId, response.id());
        assertEquals(patientId, response.patientProfileId());
        assertEquals(VisitType.OUTPATIENT, response.visitType());
        assertEquals(RecordType.CONSULTATION, response.recordType());
    }

    @Test
    void mapsChildEntitiesToResponses() {
        UUID recordId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        Instant now = Instant.now();

        Treatment treatment = new Treatment(
                UUID.randomUUID(),
                recordId,
                "LAB",
                "BLOOD_TEST",
                "CBC",
                LocalDate.now(),
                null,
                null,
                new BigDecimal("20.00"),
                new BigDecimal("18.00"),
                now,
                now
        );
        MedicalDocument document = new MedicalDocument(
                documentId,
                recordId,
                treatment.getId(),
                UUID.randomUUID(),
                patientId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                DocumentType.LAB_RESULT,
                "lab.pdf",
                100L,
                "application/pdf",
                "medical-documents",
                "patient/lab.pdf",
                "a".repeat(64),
                MedicalDocumentStatus.CONFIRMED,
                now,
                now
        );
        DocumentHashIndex hashIndex = new DocumentHashIndex(
                UUID.randomUUID(),
                "a".repeat(64),
                documentId,
                patientId,
                document.getClaimId(),
                document.getHospitalId(),
                now
        );
        HealthAccessLog accessLog = new HealthAccessLog(
                UUID.randomUUID(),
                recordId,
                documentId,
                UUID.randomUUID(),
                "DOCTOR",
                "Treatment review",
                now
        );

        assertEquals("BLOOD_TEST", mapper.toResponse(treatment).treatmentType());
        assertEquals(DocumentType.LAB_RESULT, mapper.toResponse(document).documentType());
        assertEquals(documentId, mapper.toResponse(hashIndex).documentId());
        assertEquals("DOCTOR", mapper.toResponse(accessLog).accessRole());
    }
}
