package az.saglamol.payment.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.payment.dto.request.CreateInvoiceRequest;
import az.saglamol.payment.dto.response.InvoiceResponse;
import az.saglamol.payment.entity.Invoice;
import az.saglamol.payment.entity.InvoiceStatus;
import az.saglamol.payment.exception.PaymentException;
import az.saglamol.payment.mapper.PaymentMapper;
import az.saglamol.payment.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentMapper mapper;
    private final PaymentAccessService accessService;

    public InvoiceService(InvoiceRepository invoiceRepository, PaymentMapper mapper, PaymentAccessService accessService) {
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
        this.accessService = accessService;
    }

    @Transactional
    public InvoiceResponse createInvoice(AuthContext authContext, CreateInvoiceRequest request) {
        accessService.requireCanManageInvoice(authContext, request.insuranceCompanyId(), request.hospitalId());
        Instant now = Instant.now();
        Invoice invoice = new Invoice(
                UUID.randomUUID(),
                nextInvoiceNumber(),
                request.policyId(),
                request.claimId(),
                request.insuranceCompanyId(),
                request.hospitalId(),
                request.patientProfileId(),
                request.amount(),
                request.currency() == null || request.currency().isBlank() ? "AZN" : request.currency(),
                InvoiceStatus.DRAFT,
                null,
                null,
                now,
                now
        );
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID invoiceId, AuthContext authContext) {
        Invoice invoice = invoiceById(invoiceId);
        accessService.requireCanViewInvoice(authContext, invoice);
        return mapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getByCompany(UUID companyId, Pageable pageable, AuthContext authContext) {
        if (!accessService.isAdmin(authContext)) {
            UUID scopedCompanyId = accessService.resolveCompanyScope(authContext);
            if (!companyId.equals(scopedCompanyId)) {
                throw new PaymentException("FORBIDDEN", "Current user cannot access this company invoices");
            }
        }
        return invoiceRepository.findByInsuranceCompanyId(companyId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getByHospital(UUID hospitalId, Pageable pageable, AuthContext authContext) {
        if (!accessService.isAdmin(authContext)) {
            UUID scopedHospitalId = accessService.currentHospitalId(authContext);
            if (!hospitalId.equals(scopedHospitalId)) {
                throw new PaymentException("FORBIDDEN", "Current user cannot access this hospital invoices");
            }
        }
        return invoiceRepository.findByHospitalId(hospitalId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public InvoiceResponse issueInvoice(UUID invoiceId, AuthContext authContext) {
        Invoice invoice = invoiceById(invoiceId);
        accessService.requireCanManageInvoice(authContext, invoice.getInsuranceCompanyId(), invoice.getHospitalId());
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new PaymentException("INVALID_INVOICE_STATUS", "Only draft invoices can be issued");
        }
        invoice.issue(Instant.now());
        return mapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse markPaid(UUID invoiceId, AuthContext authContext) {
        Invoice invoice = invoiceById(invoiceId);
        accessService.requireCanManageInvoice(authContext, invoice.getInsuranceCompanyId(), invoice.getHospitalId());
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new PaymentException("INVOICE_ALREADY_PAID", "Invoice is already paid");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new PaymentException("INVALID_INVOICE_STATUS", "Cancelled invoices cannot be paid");
        }
        invoice.markPaid(Instant.now());
        return mapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse cancelInvoice(UUID invoiceId, AuthContext authContext) {
        Invoice invoice = invoiceById(invoiceId);
        accessService.requireCanManageInvoice(authContext, invoice.getInsuranceCompanyId(), invoice.getHospitalId());
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new PaymentException("INVALID_INVOICE_STATUS", "Paid invoices cannot be cancelled");
        }
        invoice.cancel(Instant.now());
        return mapper.toResponse(invoice);
    }

    private Invoice invoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PaymentException("INVOICE_NOT_FOUND", "Invoice was not found"));
    }

    private String nextInvoiceNumber() {
        return "INV-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
