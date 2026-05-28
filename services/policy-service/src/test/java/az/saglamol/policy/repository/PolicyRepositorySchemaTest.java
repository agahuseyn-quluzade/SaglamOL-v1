package az.saglamol.policy.repository;

import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.PayoutModel;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyLimitReservation;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import az.saglamol.policy.entity.ReservationStatus;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.entity.ServiceType;
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
        "spring.liquibase.contexts=prod",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class PolicyRepositorySchemaTest {

    @Autowired
    private InsuranceProductRepository productRepository;

    @Autowired
    private CoverageRuleRepository coverageRuleRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private ProviderContractRepository providerContractRepository;

    @Autowired
    private PolicyLimitReservationRepository reservationRepository;

    @Test
    void repositoriesSaveAndFindPolicySchema() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        Instant now = Instant.now();

        productRepository.saveAndFlush(product(productId, companyId, "HEALTH-STANDARD"));
        coverageRuleRepository.saveAndFlush(new CoverageRule(
                UUID.randomUUID(),
                productId,
                ServiceType.HOSPITAL,
                80,
                new BigDecimal("5000.00"),
                30,
                true,
                RuleStatus.ACTIVE,
                now,
                now
        ));
        policyRepository.saveAndFlush(new Policy(
                policyId,
                "POL-1001",
                companyId,
                productId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                PolicyStatus.PAYMENT_PENDING,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                new BigDecimal("1200.00"),
                new BigDecimal("10000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                now,
                now
        ));
        providerContractRepository.saveAndFlush(new ProviderContract(
                UUID.randomUUID(),
                companyId,
                UUID.randomUUID(),
                productId,
                "CON-1001",
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                ProviderContractStatus.ACTIVE,
                PayoutModel.DIRECT_TO_HOSPITAL,
                now,
                now
        ));
        reservationRepository.saveAndFlush(new PolicyLimitReservation(
                UUID.randomUUID(),
                policyId,
                companyId,
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                ReservationStatus.RESERVED,
                now,
                now
        ));

        assertTrue(productRepository.findByInsuranceCompanyIdAndProductCode(companyId, "HEALTH-STANDARD").isPresent());
        assertEquals(1, coverageRuleRepository.findByProductId(productId).size());
        assertTrue(policyRepository.findByPolicyNumber("POL-1001").isPresent());
        assertEquals(1, providerContractRepository.findByInsuranceCompanyId(companyId).size());
        assertEquals(1, reservationRepository.findByPolicyId(policyId).size());
    }

    @Test
    void duplicateProductCodeInSameCompanyFails() {
        UUID companyId = UUID.randomUUID();

        productRepository.saveAndFlush(product(UUID.randomUUID(), companyId, "SAME-CODE"));

        assertThrows(DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(product(UUID.randomUUID(), companyId, "SAME-CODE")));
    }

    @Test
    void sameProductCodeInDifferentCompaniesIsAllowed() {
        String productCode = "SHARED-CODE";

        productRepository.saveAndFlush(product(UUID.randomUUID(), UUID.randomUUID(), productCode));
        productRepository.saveAndFlush(product(UUID.randomUUID(), UUID.randomUUID(), productCode));

        assertEquals(2, productRepository.findAll().size());
    }

    private InsuranceProduct product(UUID productId, UUID companyId, String productCode) {
        Instant now = Instant.now();
        return new InsuranceProduct(
                productId,
                companyId,
                productCode,
                "Standard Health",
                "Standard health insurance product",
                CoverageType.STANDARD,
                new BigDecimal("100.00"),
                new BigDecimal("10000.00"),
                "AZN",
                InsuranceProductStatus.ACTIVE,
                now,
                now
        );
    }
}
