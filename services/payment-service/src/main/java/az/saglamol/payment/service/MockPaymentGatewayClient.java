package az.saglamol.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockPaymentGatewayClient {

    private final double failureRate;

    public MockPaymentGatewayClient(@Value("${payment.mock.failure-rate:${PAYMENT_MOCK_FAILURE_RATE:0.0}}") double failureRate) {
        this.failureRate = failureRate;
    }

    public MockGatewayResult authorize(BigDecimal amount) {
        return execute("AUTH", amount);
    }

    public MockGatewayResult capture(BigDecimal amount) {
        return execute("CAP", amount);
    }

    public MockGatewayResult payout(BigDecimal amount) {
        return execute("PAY", amount);
    }

    public MockGatewayResult refund(BigDecimal amount) {
        return execute("REF", amount);
    }

    private MockGatewayResult execute(String prefix, BigDecimal amount) {
        boolean success = ThreadLocalRandom.current().nextDouble() >= failureRate;
        String reference = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
        String response = success ? "MOCK_SUCCESS amount=" + amount : "MOCK_FAILED amount=" + amount;
        return new MockGatewayResult(success, reference, response);
    }

    public record MockGatewayResult(boolean success, String reference, String providerResponse) {
    }
}
