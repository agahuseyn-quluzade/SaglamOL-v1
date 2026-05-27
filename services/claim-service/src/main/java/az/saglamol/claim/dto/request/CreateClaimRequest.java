package az.saglamol.claim.dto.request;

import az.saglamol.claim.entity.PayoutRecipientType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateClaimRequest(
        @NotBlank String claimNumber,
        @NotNull UUID policyId,
        @NotNull UUID insuranceCompanyId,
        @NotNull UUID patientProfileId,
        UUID hospitalId,
        UUID doctorProfileId,
        @NotBlank String serviceType,
        @NotNull LocalDate treatmentDate,
        @NotNull @DecimalMin("0.00") BigDecimal claimAmount,
        @NotNull @DecimalMin("0.00") BigDecimal coveredAmount,
        @NotNull @DecimalMin("0.00") BigDecimal patientPayAmount,
        @NotNull PayoutRecipientType payoutRecipientType,
        UUID policyReservationId,
        String diagnosisCode,
        String reason,
        String notes,
        @Valid List<CreateClaimItemRequest> items,
        @Valid List<CreateClaimDocumentReferenceRequest> documents
) {
}
