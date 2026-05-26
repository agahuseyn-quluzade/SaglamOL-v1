# Prompt 12 - Payment Service

This phase implements Payment Service for multi-company policy premium payments, claim payouts, hospital payouts, refunds, and invoices.

## Main APIs

Payments:

- `POST /api/v1/payments/policy-premium`
- `POST /api/v1/payments/claim-payout`
- `POST /api/v1/payments/{id}/complete-mock`
- `POST /api/v1/payments/{id}/fail-mock`
- `POST /api/v1/payments/{id}/refund-mock`
- `GET /api/v1/payments/{id}`
- `GET /api/v1/payments/my`
- `GET /api/v1/payments/by-policy?policyId=...`
- `GET /api/v1/payments/by-claim?claimId=...`
- `GET /api/v1/payments/by-company?companyId=...`
- `GET /api/v1/payments/by-hospital?hospitalId=...`

Invoices:

- `POST /api/v1/invoices`
- `GET /api/v1/invoices/{id}`
- `GET /api/v1/invoices/by-company?companyId=...`
- `GET /api/v1/invoices/by-hospital?hospitalId=...`
- `POST /api/v1/invoices/{id}/issue`
- `POST /api/v1/invoices/{id}/mark-paid`
- `POST /api/v1/invoices/{id}/cancel`

## Policy Activation Flow

1. Policy Service issues a policy in `PAYMENT_PENDING`.
2. Payment Service creates a `POLICY_PREMIUM` payment in `PENDING`.
3. Mock payment completion changes payment status to `COMPLETED`.
4. Payment Service calls Policy Service internal endpoint:
   `PUT /internal/v1/policies/{policyId}/activate-after-payment`
5. Payment Service writes `PaymentCompletedEvent` to the transactional outbox.

Failed mock payment changes status to `FAILED`, writes a failed transaction, and does not activate the policy.

## Payout Flow

- Claim payout and hospital payout are represented as payments with `CLAIM_PAYOUT` or `HOSPITAL_PAYOUT`.
- Completing payout writes `PaymentCompletedEvent` and `ClaimPayoutCompletedEvent`.
- Failing payout writes `PaymentFailedEvent` and `ClaimPayoutFailedEvent`.

## Refund Flow

- Only `COMPLETED` payments can be refunded.
- Refund writes a `REFUND` transaction and `RefundCompletedEvent`.

## Security Scope

- `ADMIN`: global access.
- `PATIENT`: own payments and invoices through patient profile scope.
- `AGENT`, `INSURANCE_ADMIN`, `INSURANCE_STAFF`: own insurance company scope.
- `HOSPITAL_ADMIN`: own hospital payout and invoice scope.

Ownership is resolved from `AuthContext.userId` through User Profile internal APIs.

## Mock Provider

Payment provider is `MOCK`.

Failure simulation:

```text
PAYMENT_MOCK_FAILURE_RATE=${PAYMENT_MOCK_FAILURE_RATE:0.0}
```

Tests should keep this value at `0.0` to avoid flaky results.
