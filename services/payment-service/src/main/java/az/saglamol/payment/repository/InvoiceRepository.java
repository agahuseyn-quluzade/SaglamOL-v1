package az.saglamol.payment.repository;

import az.saglamol.payment.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByInsuranceCompanyId(UUID insuranceCompanyId, Pageable pageable);

    Page<Invoice> findByHospitalId(UUID hospitalId, Pageable pageable);
}
