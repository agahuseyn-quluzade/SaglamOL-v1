package az.saglamol.policy.service;

import az.saglamol.policy.dto.request.EligibilityCheckRequest;
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
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.PolicyLimitReservationRepository;
import az.saglamol.policy.repository.PolicyRepository;
import az.saglamol.policy.repository.ProviderContractRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EligibilityAndLimitServiceTest {

    private final PolicyRepository policyRepository = mock(PolicyRepository.class);
    private final InsuranceProductRepository productRepository = mock(InsuranceProductRepository.class);
    private final CoverageRuleRepository coverageRuleRepository = mock(CoverageRuleRepository.class);
    private final ProviderContractRepository contractRepository = mock(ProviderContractRepository.class);
    private final EligibilityService eligibilityService = new EligibilityService(
            policyRepository,
            productRepository,
            coverageRuleRepository,
            contractRepository
    );
    private final PolicyLimitReservationRepository reservationRepository = mock(PolicyLimitReservationRepository.class);
    private final PolicyMapper mapper = Mappers.getMapper(PolicyMapper.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final PolicyLimitService limitService = new PolicyLimitService(
            policyRepository,
            reservationRepository,
            mapper,
            transactionTemplate
    );

    @Test
    void eligibilityInNetworkPassesAndCalculatesCoveredAmount() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        mockEligibility(policy(policyId, companyId, productId, patientProfileId, PolicyStatus.ACTIVE),
                product(productId, companyId, InsuranceProductStatus.ACTIVE),
                rule(productId, 80, new BigDecimal("500.00"), 0),
                List.of(contract(companyId, hospitalId, productId, ProviderContractStatus.ACTIVE)));

        var response = eligibilityService.checkEligibility(new EligibilityCheckRequest(
                policyId,
                companyId,
                patientProfileId,
                hospitalId,
                ServiceType.HOSPITAL,
                new BigDecimal("1000.00"),
                LocalDate.now()
        ));

        assertTrue(response.eligible());
        assertTrue(response.inNetwork());
        assertEquals(new BigDecimal("500.00"), response.coveredAmount());
    }

    @Test
    void eligibilityOutOfNetworkFails() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        mockEligibility(policy(policyId, companyId, productId, patientProfileId, PolicyStatus.ACTIVE),
                product(productId, companyId, InsuranceProductStatus.ACTIVE),
                rule(productId, 80, new BigDecimal("5000.00"), 0),
                List.of());

        var response = eligibilityService.checkEligibility(new EligibilityCheckRequest(
                policyId,
                companyId,
                patientProfileId,
                hospitalId,
                ServiceType.HOSPITAL,
                new BigDecimal("1000.00"),
                LocalDate.now()
        ));

        assertFalse(response.eligible());
        assertTrue(response.reasons().contains("OUT_OF_NETWORK_PROVIDER"));
    }

    @Test
    void eligibilityWaitingPeriodFails() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        mockEligibility(policy(policyId, companyId, productId, patientProfileId, PolicyStatus.ACTIVE),
                product(productId, companyId, InsuranceProductStatus.ACTIVE),
                rule(productId, 80, new BigDecimal("5000.00"), 30),
                List.of());

        var response = eligibilityService.checkEligibility(new EligibilityCheckRequest(
                policyId,
                companyId,
                patientProfileId,
                null,
                ServiceType.HOSPITAL,
                new BigDecimal("1000.00"),
                LocalDate.now()
        ));

        assertFalse(response.eligible());
        assertTrue(response.reasons().contains("WAITING_PERIOD_NOT_PASSED"));
    }

    @Test
    void eligibilityLimitExceededFails() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        mockEligibility(policyWithLimits(policyId, companyId, productId, patientProfileId, PolicyStatus.ACTIVE,
                        new BigDecimal("1000.00"), new BigDecimal("900.00"), new BigDecimal("50.00")),
                product(productId, companyId, InsuranceProductStatus.ACTIVE),
                rule(productId, 80, new BigDecimal("5000.00"), 0),
                List.of());

        var response = eligibilityService.checkEligibility(new EligibilityCheckRequest(
                policyId,
                companyId,
                patientProfileId,
                null,
                ServiceType.HOSPITAL,
                new BigDecimal("100.00"),
                LocalDate.now()
        ));

        assertFalse(response.eligible());
        assertTrue(response.reasons().contains("LIMIT_EXCEEDED"));
    }

    @Test
    void reserveRetriesOnOptimisticLockFailure() {
        UUID policyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        AtomicInteger attempts = new AtomicInteger();
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() == 1) {
                throw new OptimisticLockingFailureException("retry");
            }
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy(policyId, companyId, UUID.randomUUID(), UUID.randomUUID(), PolicyStatus.ACTIVE)));
        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = limitService.reserveLimit(policyId, claimId, companyId, new BigDecimal("100.00"));

        assertEquals(ReservationStatus.RESERVED, response.status());
        verify(transactionTemplate, times(2)).execute(any());
    }

    @Test
    void duplicateReservedClaimFails() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        UUID claimId = UUID.randomUUID();
        when(reservationRepository.existsByClaimIdAndStatus(claimId, ReservationStatus.RESERVED)).thenReturn(true);

        assertThrows(PolicyException.class, () -> limitService.reserveLimit(
                UUID.randomUUID(),
                claimId,
                UUID.randomUUID(),
                new BigDecimal("100.00")
        ));
    }

    @Test
    void commitAndReleaseReservationTransitions() {
        UUID policyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        Policy policy = policy(policyId, companyId, UUID.randomUUID(), UUID.randomUUID(), PolicyStatus.ACTIVE);
        policy.reserveLimit(new BigDecimal("100.00"), Instant.now());
        PolicyLimitReservation commitReservation = reservation(UUID.randomUUID(), policyId, companyId, claimId, ReservationStatus.RESERVED);
        PolicyLimitReservation releaseReservation = reservation(UUID.randomUUID(), policyId, companyId, UUID.randomUUID(), ReservationStatus.RESERVED);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(reservationRepository.findById(commitReservation.getId())).thenReturn(Optional.of(commitReservation));
        when(reservationRepository.findById(releaseReservation.getId())).thenReturn(Optional.of(releaseReservation));

        var committed = limitService.commitReservation(commitReservation.getId());
        policy.reserveLimit(new BigDecimal("100.00"), Instant.now());
        var released = limitService.releaseReservation(releaseReservation.getId(), "not needed");

        assertEquals(ReservationStatus.COMMITTED, committed.status());
        assertEquals(ReservationStatus.RELEASED, released.status());
    }

    private void mockEligibility(Policy policy, InsuranceProduct product, CoverageRule rule, List<ProviderContract> contracts) {
        when(policyRepository.findById(policy.getId())).thenReturn(Optional.of(policy));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(coverageRuleRepository.findByProductIdAndServiceTypeAndStatus(product.getId(), ServiceType.HOSPITAL, RuleStatus.ACTIVE))
                .thenReturn(Optional.of(rule));
        when(contractRepository.findByInsuranceCompanyIdAndHospitalIdAndStatus(
                policy.getInsuranceCompanyId(),
                contracts.isEmpty() ? null : contracts.getFirst().getHospitalId(),
                ProviderContractStatus.ACTIVE
        )).thenReturn(contracts);
        if (contracts.isEmpty()) {
            when(contractRepository.findByInsuranceCompanyIdAndHospitalIdAndStatus(any(), any(), any())).thenReturn(List.of());
        }
    }

    private InsuranceProduct product(UUID productId, UUID companyId, InsuranceProductStatus status) {
        Instant now = Instant.now();
        return new InsuranceProduct(productId, companyId, "P-1", "Standard", null, CoverageType.STANDARD,
                new BigDecimal("100.00"), new BigDecimal("10000.00"), "AZN", status, now, now);
    }

    private CoverageRule rule(UUID productId, int percent, BigDecimal maxAmount, int waitingPeriodDays) {
        Instant now = Instant.now();
        return new CoverageRule(UUID.randomUUID(), productId, ServiceType.HOSPITAL, percent, maxAmount,
                waitingPeriodDays, false, RuleStatus.ACTIVE, now, now);
    }

    private ProviderContract contract(UUID companyId, UUID hospitalId, UUID productId, ProviderContractStatus status) {
        Instant now = Instant.now();
        return new ProviderContract(UUID.randomUUID(), companyId, hospitalId, productId, "CON-1",
                LocalDate.now().minusDays(10), LocalDate.now().plusYears(1), status, PayoutModel.DIRECT_TO_HOSPITAL, now, now);
    }

    private Policy policy(UUID policyId, UUID companyId, UUID productId, UUID patientProfileId, PolicyStatus status) {
        return policyWithLimits(policyId, companyId, productId, patientProfileId, status,
                new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private Policy policyWithLimits(UUID policyId, UUID companyId, UUID productId, UUID patientProfileId, PolicyStatus status,
                                    BigDecimal annualLimit, BigDecimal usedLimit, BigDecimal reservedLimit) {
        Instant now = Instant.now();
        return new Policy(policyId, "POL-1", companyId, productId, patientProfileId, null, status,
                LocalDate.now().minusDays(1), LocalDate.now().plusYears(1), new BigDecimal("100.00"),
                annualLimit, usedLimit, reservedLimit, now, now);
    }

    private PolicyLimitReservation reservation(UUID reservationId, UUID policyId, UUID companyId, UUID claimId, ReservationStatus status) {
        Instant now = Instant.now();
        return new PolicyLimitReservation(reservationId, policyId, companyId, claimId, new BigDecimal("100.00"), status, now, now);
    }
}
