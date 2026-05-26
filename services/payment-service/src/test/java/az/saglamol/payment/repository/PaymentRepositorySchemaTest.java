package az.saglamol.payment.repository;

import az.saglamol.payment.entity.Payment;
import az.saglamol.payment.entity.PaymentProvider;
import az.saglamol.payment.entity.PaymentStatus;
import az.saglamol.payment.entity.PaymentTransaction;
import az.saglamol.payment.entity.PaymentType;
import az.saglamol.payment.entity.TransactionStatus;
import az.saglamol.payment.entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.xml",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class PaymentRepositorySchemaTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Test
    void repositoriesSaveAndFindPaymentSchema() {
        UUID companyId = UUID.randomUUID();
        Instant now = Instant.now();
        Payment payment = paymentRepository.save(new Payment(
                UUID.randomUUID(),
                "PAY-TEST-1",
                UUID.randomUUID(),
                companyId,
                UUID.randomUUID(),
                null,
                null,
                new BigDecimal("120.00"),
                "AZN",
                PaymentType.POLICY_PREMIUM,
                PaymentStatus.PENDING,
                PaymentProvider.MOCK,
                null,
                now,
                now
        ));
        transactionRepository.save(new PaymentTransaction(
                UUID.randomUUID(),
                payment.getId(),
                TransactionType.AUTHORIZE,
                payment.getAmount(),
                TransactionStatus.SUCCESS,
                "ok",
                now
        ));

        assertEquals(1, paymentRepository.findByInsuranceCompanyId(companyId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, transactionRepository.findByPaymentId(payment.getId()).size());
    }
}
