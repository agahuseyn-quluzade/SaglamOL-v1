# P15 Claim Kafka, Feign, Outbox

## Kafka Consumers

Claim Service listens with `claim-service-group`:

- `risk.events`: `RiskAnalysisCompletedEvent`
- `fraud.events`: `FraudCheckCompletedEvent`
- `payment.events`: `ClaimPayoutCompletedEvent`, `ClaimPayoutFailedEvent`

Consumers extend `IdempotentEventConsumer` and use `processed_events` through `ProcessedEventService`.

Risk updates:

- `riskScore`
- `riskLevel`

Fraud updates:

- `fraudScore`
- `fraudLevel`
- `fraudPassed`

Payment updates:

- `ClaimPayoutCompletedEvent`: `APPROVED -> PAID`
- `ClaimPayoutFailedEvent`: sets `PAYOUT_FAILED` marker unless claim is already `PAID` or `CANCELLED`

## Outbox Worker

`ClaimOutboxConfig` wires `OutboxPublisherScheduler<OutboxEvent>` with service name `claim-service`.

The scheduler uses common-kafka:

```yaml
outbox:
  topic: claim.events
  max-retries: 5
  scheduler:
    enabled: true
    interval: 5000
```

## Feign Clients

Feign clients use Eureka service names:

- `policy-service`: `/internal/v1/policies`
- `user-profile-service`: `/internal/v1/profiles`
- `health-record-service`: `/internal/v1/health-records`

Internal headers are added automatically:

- `X-Internal-Service-Secret`
- `X-Correlation-Id`

Timeouts:

```yaml
claim:
  feign:
    connect-timeout-ms: 2000
    read-timeout-ms: 5000
```

## Kafka Config

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: claim-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```
