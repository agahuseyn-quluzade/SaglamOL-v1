# SaglamOL

SaglamOL is a multi-company e-health insurance backend built as a Spring Boot and Gradle multi-module microservice platform. It covers IAM, user profiles, insurance policies, claims, payments, health records, AI risk assessment, fraud detection, notifications, and supporting infrastructure.

## Architecture

The platform follows an event-driven microservice architecture:

- Spring Boot services with isolated domain ownership.
- PostgreSQL-backed persistence with Liquibase migrations.
- Kafka integration through transactional outbox publishing and idempotent consumers.
- Eureka service discovery and Spring Cloud Config.
- Internal service-to-service APIs protected by `X-Internal-Service-Secret`.
- MinIO-backed medical document storage.
- Mock payment and notification senders for local development.
- External AI risk integration with deterministic fallback behavior.

## Modules

| Module | Responsibility |
| --- | --- |
| `common/common-events` | Shared event contracts and Kafka topic constants |
| `common/common-kafka` | Outbox publishing, idempotent consumer support, DLT handling |
| `common/common-security` | Shared auth context and role constants |
| `common/common-exception` | Shared exception helpers |
| `infrastructure/api-gateway` | Edge gateway |
| `infrastructure/config-server` | Centralized configuration |
| `infrastructure/discovery-server` | Eureka service discovery |
| `services/iam-service` | Users, roles, auth, password reset |
| `services/user-profile-service` | Patient, doctor, hospital, agent, insurance company profiles |
| `services/policy-service` | Policy lifecycle, eligibility, limit reservations |
| `services/claim-service` | Claim lifecycle, review, async risk/fraud/payment updates |
| `services/health-record-service` | Health records, treatments, MinIO medical documents |
| `services/ai-risk-service` | External AI risk scoring and fallback model |
| `services/fraud-detection-service` | Rule-based fraud assessment |
| `services/payment-service` | Premium payments, claim payouts, mock gateway |
| `services/notification-service` | Templates, mock senders, event-driven notifications |

## Event Flow

Core claim processing is asynchronous:

1. Claim Service publishes `ClaimSubmittedEvent`.
2. AI Risk and Fraud services consume it independently and publish completion events.
3. Claim Service updates risk and fraud fields without blocking claim submission.
4. Claim approval publishes `ClaimApprovedEvent`.
5. Payment Service consumes the approval and creates a claim payout once.
6. Payment completion publishes payout events.
7. Claim Service marks the claim paid.
8. Notification Service consumes profile, policy, payment, claim, and explicit notification events.

See [docs/event-flow-p21.md](docs/event-flow-p21.md) for the event mapping table and DLT topic list.

## Local Development

Copy environment defaults:

```powershell
Copy-Item .env.example .env
```

Build all modules:

```powershell
.\gradlew.bat clean build
```

Start the local runtime:

```powershell
docker compose up -d --build
docker compose ps
```

Follow logs for a service:

```powershell
docker compose logs -f claim-service
```

Stop the stack:

```powershell
docker compose down
```

Reset local volumes:

```powershell
docker compose down -v
```

## Useful Test Commands

Run all tests:

```powershell
.\gradlew.bat test
```

Run core event-flow service tests:

```powershell
.\gradlew.bat :services:claim-service:test :services:payment-service:test :services:notification-service:test :services:ai-risk-service:test :services:fraud-detection-service:test
```

Run profile and IAM tests:

```powershell
.\gradlew.bat :services:iam-service:test :services:user-profile-service:test
```

## Main Ports

| Service | Port |
| --- | --- |
| API Gateway | `8080` |
| IAM Service | `8081` |
| User Profile Service | `8082` |
| Policy Service | `8083` |
| Claim Service | `8084` |
| Health Record Service | `8085` |
| AI Risk Service | `8086` |
| Fraud Detection Service | `8087` |
| Notification Service | `8088` |
| Payment Service | `8089` |
| Discovery Server | `8761` |
| Config Server | `8888` |
| PostgreSQL | `5432` |
| Redis | `6379` |
| Kafka | `9092` |
| MinIO API | `9000` |
| MinIO Console | `9001` |

## Key Configuration

| Variable | Purpose |
| --- | --- |
| `INTERNAL_SERVICE_SECRET` | Shared internal API secret |
| `IAM_JWT_SECRET` | JWT signing secret for local/runtime auth |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers |
| `MINIO_ENDPOINT` | MinIO API endpoint |
| `AI_PROVIDER_BASE_URL` | External AI provider base URL |
| `AI_API_KEY` | External AI provider API key |
| `AI_MODEL_NAME` | AI model name |
| `AI_ENABLED` | Enables external AI calls when `true`, fallback when `false` |

## Documentation

- [docs/event-flow-p21.md](docs/event-flow-p21.md)
- [docs/claim-lifecycle.md](docs/claim-lifecycle.md)
- [docs/claim-p15-kafka-feign-outbox.md](docs/claim-p15-kafka-feign-outbox.md)
- [docs/health-record-p17-minio-api.md](docs/health-record-p17-minio-api.md)
- [docs/fraud-p18-scoring.md](docs/fraud-p18-scoring.md)
- [docs/transactional-outbox-idempotent-consumer.md](docs/transactional-outbox-idempotent-consumer.md)

## Operational Notes

- All database migrations are managed by Liquibase.
- Consumer idempotency is backed by `processed_events`.
- Outbox publishing is backed by `outbox_events`.
- Kafka listener failures retry and then publish to `<topic>.dlt`.
- Local notification and payment integrations are intentionally mock implementations.
