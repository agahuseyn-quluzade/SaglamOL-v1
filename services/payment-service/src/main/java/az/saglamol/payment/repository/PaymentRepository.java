package az.saglamol.payment.repository;

import az.saglamol.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByPatientProfileId(UUID patientProfileId);

    List<Payment> findByPolicyId(UUID policyId);

    List<Payment> findByClaimId(UUID claimId);

    Page<Payment> findByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    Page<Payment> findByHospitalId(UUID hospitalId, Pageable pageable);
}
