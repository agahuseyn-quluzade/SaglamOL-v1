package az.saglamol.policy.mapper;

import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyMapperTest {

    private final PolicyMapper mapper = Mappers.getMapper(PolicyMapper.class);

    @Test
    void mapsInsuranceProductToResponse() {
        UUID productId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Instant now = Instant.now();
        InsuranceProduct product = new InsuranceProduct(
                productId,
                companyId,
                "HEALTH-PREMIUM",
                "Premium Health",
                "Premium package",
                CoverageType.PREMIUM,
                new BigDecimal("250.00"),
                new BigDecimal("25000.00"),
                "AZN",
                InsuranceProductStatus.ACTIVE,
                now,
                now
        );

        var response = mapper.toResponse(product);

        assertEquals(productId, response.id());
        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals("HEALTH-PREMIUM", response.productCode());
        assertEquals(CoverageType.PREMIUM, response.coverageType());
    }
}
