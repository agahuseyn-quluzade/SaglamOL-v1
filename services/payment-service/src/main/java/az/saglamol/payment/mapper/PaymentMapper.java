package az.saglamol.payment.mapper;

import az.saglamol.payment.dto.response.InvoiceResponse;
import az.saglamol.payment.dto.response.PaymentResponse;
import az.saglamol.payment.dto.response.PaymentTransactionResponse;
import az.saglamol.payment.entity.Invoice;
import az.saglamol.payment.entity.Payment;
import az.saglamol.payment.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getPolicyId(),
                payment.getInsuranceCompanyId(),
                payment.getPatientProfileId(),
                payment.getClaimId(),
                payment.getHospitalId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentType(),
                payment.getStatus(),
                payment.getProvider(),
                payment.getProviderReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    public PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getPaymentId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getProviderResponse(),
                transaction.getCreatedAt()
        );
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPolicyId(),
                invoice.getClaimId(),
                invoice.getInsuranceCompanyId(),
                invoice.getHospitalId(),
                invoice.getPatientProfileId(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getPaidAt(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
