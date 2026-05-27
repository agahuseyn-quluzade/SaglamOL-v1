# Prompt 18 - Fraud Detection Scoring

Fraud Detection Service evaluates submitted claims with company-scoped, rule-based scoring. Each check creates a `FraudAssessment` in `PENDING`, builds a `FraudContext` from Claim, Policy and Health Record internal APIs, runs all `FraudRule` implementations, stores generated `FraudSignal` rows, marks the assessment `COMPLETED`, and writes a `FraudCheckCompletedEvent` to the outbox.

## Score Model

Each rule returns either no result or a `FraudRuleResult` with:

- `signalType`
- `severity`
- `scoreImpact`
- `message`

The total fraud score is the sum of impacts capped at `1.0000`.

Levels:

- `LOW`: score < `0.2500`
- `MEDIUM`: score >= `0.2500`
- `HIGH`: score >= `0.5000`
- `CRITICAL`: score >= `0.8000`

`HIGH` and `CRITICAL` assessments require manual review. `FraudCheckCompletedEvent.passed` is `false` when manual review is required.

## Rules

- `DuplicateDocumentRule`: checks Health Record document hashes against `document_hash_index`.
- `FrequentClaimsRule`: flags patients with 3 or more assessments in the last 30 days.
- `HighAmountRule`: flags claims above 3x recent company claim average from Claim Internal API.
- `SuspiciousTimingRule`: flags treatment within 7 days of policy start.
- `WaitingPeriodRule`: flags treatment within 30 days of policy start.
- `HospitalAnomalyRule`: flags hospitals with repeated recent high/critical assessments.
- `DoctorAnomalyRule`: flags doctors with repeated recent high/critical assessments.

## Security

- `ADMIN`: global access.
- `INSURANCE_ADMIN`, `AGENT`, `INSURANCE_STAFF`: scoped to own insurance company.
- `HOSPITAL_ADMIN`: scoped to own hospital.
- `PATIENT`: forbidden.

## APIs

- `POST /fraud/claims/{claimId}/check`
- `GET /fraud/claims/{claimId}`
- `GET /fraud/assessments/{assessmentId}`
- `GET /fraud/companies/{companyId}/summary`
- `GET /fraud/hospitals/{hospitalId}/summary`

## Kafka

`ClaimSubmittedConsumer` consumes `ClaimSubmittedEvent` from claim events using `IdempotentEventConsumer`. Duplicate envelopes are skipped through `processed_events`.

Outbox publishes pending `FraudCheckCompletedEvent` records through the common Kafka outbox scheduler.
