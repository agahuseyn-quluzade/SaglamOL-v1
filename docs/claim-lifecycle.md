# Claim Lifecycle

## States

`DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED`

`DRAFT -> SUBMITTED -> UNDER_REVIEW -> REJECTED`

`SUBMITTED -> UNDER_REVIEW -> NEEDS_MORE_DOCUMENTS`

`DRAFT -> CANCELLED`

`SUBMITTED -> CANCELLED`

Payment states (`PAYMENT_PENDING`, `PAID`, `PAYOUT_FAILED`) are reserved for the payment integration flow.

## Submit Flow

Claims can be submitted only from `DRAFT`.

Submission requires at least one claim item. `claimAmount` is recalculated from items as `sum(amount * quantity)`.

Policy detail is read through `PolicyInternalClient`; `insuranceCompanyId` and `patientProfileId` are validated against the policy. Eligibility is checked before any limit reservation. If eligibility fails, submission is blocked.

When eligible, Claim Service stores:

- `coveredAmount`
- `patientPayAmount`
- `payoutRecipientType`
- `policyReservationId`
- status history entry
- `ClaimSubmittedEvent` in `outbox_events`

## Review Flow

Review users can operate only on claims for their insurance company unless they have `ADMIN`.

`startReview` moves `SUBMITTED -> UNDER_REVIEW`.

`approveClaim` requires `UNDER_REVIEW`, validates `approvedAmount <= coveredAmount`, commits the reservation, writes `ClaimDecision`, moves the claim to `APPROVED`, and writes `ClaimApprovedEvent`.

`rejectClaim` requires `UNDER_REVIEW` and a non-blank reason, releases the reservation, writes `ClaimDecision`, moves the claim to `REJECTED`, and writes `ClaimRejectedEvent`.

Claim Service does not write `ClaimPayoutRequestedEvent`; payment request creation belongs to the Payment Service consumer for `ClaimApprovedEvent`.

## Access Rules

- `PATIENT`: own patient profile claims.
- `HOSPITAL_STAFF`: claims for own hospital.
- `AGENT`: review/search claims for own insurance company.
- `INSURANCE_ADMIN` and `INSURANCE_STAFF`: claims for own insurance company.
- `ADMIN`: global access.

Profile scope is resolved through `ProfileInternalClient.userSummary(iamUserId)`.
