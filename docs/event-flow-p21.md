# Prompt 21 - Event Integration and Cross-service Flow

## Core Sequence

1. Claim Service submits a claim and writes `ClaimSubmittedEvent` to the outbox. The submit response does not wait for AI Risk or Fraud.
2. AI Risk Service consumes `ClaimSubmittedEvent`, creates an AI risk assessment, and publishes `RiskAnalysisCompletedEvent`.
3. Fraud Detection Service consumes `ClaimSubmittedEvent`, creates a fraud assessment, and publishes `FraudCheckCompletedEvent`.
4. Claim Service consumes risk and fraud completion events and updates claim risk/fraud fields asynchronously.
5. Claim Review approves or rejects the claim and publishes `ClaimApprovedEvent` or `ClaimRejectedEvent`.
6. Payment Service consumes `ClaimApprovedEvent` idempotently and creates one claim payout payment.
7. Payment completion publishes `PaymentCompletedEvent` and, for claim payouts, `ClaimPayoutCompletedEvent`.
8. Claim Service consumes `ClaimPayoutCompletedEvent`, marks the claim `PAID`, records status history, and publishes `ClaimPaidEvent`.
9. Notification Service consumes profile, policy, payment, claim and explicit notification events and creates notifications idempotently.

Every consumer extends `IdempotentEventConsumer` and records processed envelope IDs in `processed_events`. Kafka error handlers retry and publish exhausted records to `<topic>.dlt`.

## Event Mapping

| Publisher | Event | Topic | Consumers | Effect |
|---|---|---|---|---|
| IAM | `UserRoleAssignedEvent` | `iam.events` | Future profile/notification consumers | Role assignment integration event |
| User Profile | `InsuranceCompanyCreatedEvent` | `profile.events` | Notification | Company-created notification |
| User Profile | `InsuranceCompanyActivatedEvent` | `profile.events` | Downstream services | Company activation integration event |
| User Profile | `HospitalCreatedEvent` | `profile.events` | Notification/downstream services | Hospital-created integration event |
| User Profile | `HospitalStaffCreatedEvent` | `profile.events` | Downstream services | Hospital staff integration event |
| User Profile | `InsuranceCompanyStaffCreatedEvent` | `profile.events` | Downstream services | Insurance staff integration event |
| Policy | `PolicyCreatedEvent` | `policy.events` | Notification | Policy-created notification |
| Policy | `PolicyActivatedEvent` | `policy.events` | Notification | Policy-activated notification |
| Policy | `PolicyCancelledEvent` | `policy.events` | Downstream services | Policy cancellation integration event |
| Claim | `ClaimSubmittedEvent` | `claim.events` | AI Risk, Fraud, Notification | Async assessments and submitted notification |
| Claim | `ClaimApprovedEvent` | `claim.events` | Payment, Notification | Creates payout payment and approved notification |
| Claim | `ClaimRejectedEvent` | `claim.events` | Notification | Rejected notification |
| Claim | `ClaimPaidEvent` | `claim.events` | Downstream services | Paid claim integration event |
| Payment | `PaymentCompletedEvent` | `payment.events` | Notification | Payment-completed notification |
| Payment | `PaymentFailedEvent` | `payment.events` | Notification | Payment-failed notification |
| Payment | `ClaimPayoutCompletedEvent` | `payment.events` | Claim | Claim marked `PAID` |
| Payment | `ClaimPayoutFailedEvent` | `payment.events` | Claim | Claim marked `PAYOUT_FAILED` |
| Health Record | `MedicalDocumentConfirmedEvent` | `health-record.events` | Downstream services | Document confirmed integration event |
| AI Risk | `RiskAnalysisCompletedEvent` | `risk.events` | Claim | Claim risk fields updated |
| Fraud | `FraudCheckCompletedEvent` | `fraud.events` | Claim | Claim fraud fields updated |
| Notification | `NotificationRequestedEvent` | `notification.events` | Notification | Explicit notification creation |

## DLT Topics

All event topics use `.dlt` suffix after retry exhaustion:

- `iam.events.dlt`
- `profile.events.dlt`
- `policy.events.dlt`
- `payment.events.dlt`
- `claim.events.dlt`
- `health-record.events.dlt`
- `risk.events.dlt`
- `fraud.events.dlt`
- `notification.events.dlt`
