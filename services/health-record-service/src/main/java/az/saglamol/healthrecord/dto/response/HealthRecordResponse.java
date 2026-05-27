package az.saglamol.healthrecord.dto.response;

import az.saglamol.healthrecord.entity.HealthRecordStatus;
import az.saglamol.healthrecord.entity.RecordType;
import az.saglamol.healthrecord.entity.VisitType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HealthRecordResponse(
        UUID id,
        UUID patientProfileId,
        UUID doctorProfileId,
        UUID hospitalId,
        UUID branchId,
        UUID claimId,
        LocalDate visitDate,
        VisitType visitType,
        RecordType recordType,
        String diagnosis,
        String notes,
        HealthRecordStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
