package az.saglamol.policy.service;

import az.saglamol.policy.dto.response.PolicyLimitReservationResponse;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyLimitReservation;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.entity.ReservationStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.PolicyLimitReservationRepository;
import az.saglamol.policy.repository.PolicyRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PolicyLimitService {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    private final PolicyRepository policyRepository;
    private final PolicyLimitReservationRepository reservationRepository;
    private final PolicyMapper policyMapper;
    private final TransactionTemplate transactionTemplate;

    public PolicyLimitService(
            PolicyRepository policyRepository,
            PolicyLimitReservationRepository reservationRepository,
            PolicyMapper policyMapper,
            TransactionTemplate transactionTemplate
    ) {
        this.policyRepository = policyRepository;
        this.reservationRepository = reservationRepository;
        this.policyMapper = policyMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public PolicyLimitReservationResponse reserveLimit(UUID policyId, UUID claimId, UUID companyId, BigDecimal amount) {
        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> reserveLimitOnce(policyId, claimId, companyId, amount));
            } catch (OptimisticLockingFailureException exception) {
                if (attempt == MAX_OPTIMISTIC_RETRIES) {
                    throw exception;
                }
            }
        }
        throw new PolicyException("LIMIT_RESERVATION_FAILED", "Policy limit reservation failed");
    }

    private PolicyLimitReservationResponse reserveLimitOnce(UUID policyId, UUID claimId, UUID companyId, BigDecimal amount) {
        if (reservationRepository.existsByClaimIdAndStatus(claimId, ReservationStatus.RESERVED)) {
            throw new PolicyException("LIMIT_ALREADY_RESERVED", "Claim already has a reserved policy limit");
        }
        Policy policy = policyById(policyId);
        requireActiveCompanyPolicy(policy, companyId);
        if (policy.availableLimit().compareTo(amount) < 0) {
            throw new PolicyException("LIMIT_EXCEEDED", "Policy available limit is lower than requested reservation");
        }
        Instant now = Instant.now();
        policy.reserveLimit(amount, now);
        policyRepository.saveAndFlush(policy);
        PolicyLimitReservation reservation = new PolicyLimitReservation(
                UUID.randomUUID(),
                policyId,
                companyId,
                claimId,
                amount,
                ReservationStatus.RESERVED,
                now,
                now
        );
        return policyMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public PolicyLimitReservationResponse commitReservation(UUID reservationId) {
        PolicyLimitReservation reservation = reservationById(reservationId);
        requireReserved(reservation);
        Policy policy = policyById(reservation.getPolicyId());
        Instant now = Instant.now();
        policy.commitReservedLimit(reservation.getReservedAmount(), now);
        reservation.commit(now);
        return policyMapper.toResponse(reservation);
    }

    @Transactional
    public PolicyLimitReservationResponse releaseReservation(UUID reservationId, String reason) {
        PolicyLimitReservation reservation = reservationById(reservationId);
        requireReserved(reservation);
        Policy policy = policyById(reservation.getPolicyId());
        Instant now = Instant.now();
        policy.releaseReservedLimit(reservation.getReservedAmount(), now);
        reservation.release(now);
        return policyMapper.toResponse(reservation);
    }

    private void requireActiveCompanyPolicy(Policy policy, UUID companyId) {
        if (!policy.getInsuranceCompanyId().equals(companyId)) {
            throw new PolicyException("INSURANCE_COMPANY_MISMATCH", "Policy is outside insurance company scope");
        }
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new PolicyException("POLICY_NOT_ACTIVE", "Policy must be active before limit reservation");
        }
    }

    private void requireReserved(PolicyLimitReservation reservation) {
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new PolicyException("INVALID_RESERVATION_STATUS", "Only reserved policy limits can be changed");
        }
    }

    private Policy policyById(UUID policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException("POLICY_NOT_FOUND", "Policy was not found"));
    }

    private PolicyLimitReservation reservationById(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new PolicyException("LIMIT_RESERVATION_NOT_FOUND", "Policy limit reservation was not found"));
    }
}
