package az.saglamol.payment.client;

import java.util.UUID;

public interface PolicyInternalClient {

    void activateAfterPayment(UUID policyId);
}
