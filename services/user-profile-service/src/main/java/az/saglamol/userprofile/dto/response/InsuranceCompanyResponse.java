package az.saglamol.userprofile.dto.response;

import az.saglamol.userprofile.entity.InsuranceCompanyStatus;

import java.time.Instant;
import java.util.UUID;

public record InsuranceCompanyResponse(
        UUID id,
        String name,
        String taxId,
        String licenseNumber,
        String email,
        String phone,
        InsuranceCompanyStatus status,
        Instant createdAt
) {
}
