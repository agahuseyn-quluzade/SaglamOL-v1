package az.saglamol.healthrecord.dto.request;

import az.saglamol.healthrecord.entity.RecordType;
import az.saglamol.healthrecord.entity.VisitType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateHealthRecordRequest(
        @NotNull UUID patientProfileId,
        UUID doctorProfileId,
        UUID hospitalId,
        UUID branchId,
        UUID claimId,
        @NotNull LocalDate visitDate,
        @NotNull VisitType visitType,
        @NotNull RecordType recordType,
        String diagnosis,
        String notes
) {
}
