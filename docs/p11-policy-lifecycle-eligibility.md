# Prompt 11 - Policy Lifecycle, Eligibility, Limits

This phase completes the main policy runtime flow for the multi-company SaglamOL platform.

## Public APIs

- `POST /api/v1/policies` - issue a policy in `PAYMENT_PENDING` status.
- `GET /api/v1/policies/{policyId}` - read a policy with ownership checks.
- `GET /api/v1/policies/me` - read current patient's policies.
- `GET /api/v1/policies` - search policies by company, patient, and status.
- `POST /api/v1/policies/eligibility-check` - calculate claim eligibility.
- `PATCH /api/v1/policies/{policyId}/cancel` - cancel a policy.
- `PATCH /api/v1/policies/{policyId}/suspend` - suspend a policy.

## Internal APIs

All internal APIs require `X-Internal-Service-Secret`.

- `GET /internal/v1/policies/{policyId}`
- `GET /internal/v1/policies/{policyId}/active`
- `POST /internal/v1/policies/eligibility-check`
- `POST /internal/v1/policies/{policyId}/limit-reservations`
- `PUT /internal/v1/policies/limit-reservations/{reservationId}/confirm`
- `PUT /internal/v1/policies/limit-reservations/{reservationId}/release`
- `PUT /internal/v1/policies/{policyId}/activate-after-payment`

## Lifecycle Rules

- A policy can be issued only from an `ACTIVE` insurance product.
- `insuranceCompanyId`, premium, and limits are copied from the product.
- New policies start as `PAYMENT_PENDING`.
- Payment Service activates the policy through the internal activation endpoint.
- `PolicyCreatedEvent`, `PolicyActivatedEvent`, and `PolicyCancelledEvent` are saved through the transactional outbox.

## Eligibility Calculation

Eligibility is approved only when all required rules pass:

- Policy status is `ACTIVE`.
- Request `insuranceCompanyId` and `patientProfileId` match the policy.
- Treatment date is inside policy start/end dates.
- Product status is `ACTIVE`.
- Matching `CoverageRule` exists and is `ACTIVE`.
- Waiting period has passed.
- `availableLimit = annualLimit - usedLimit - reservedLimit` is enough for claim amount.
- If `hospitalId` is provided, an active provider contract must cover the hospital and product.

Covered amount:

```text
coveredByPercent = claimAmount * coveragePercent / 100
coveredAmount = min(coveredByPercent, coverageRule.maxAmount)
```

## Limit Reservation

- A claim can have only one active `RESERVED` reservation.
- Reservation uses optimistic locking retry.
- Confirming a reservation moves reserved amount into used limit.
- Releasing a reservation returns the amount to available limit.
