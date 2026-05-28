# SaglamOL

SaglamOL is a demo-ready, multi-company e-health insurance platform built with Spring Boot, Gradle, PostgreSQL, Kafka, Redis, MinIO, Eureka, Spring Cloud Config, Prometheus and Grafana.

The system models insurance companies, hospitals, doctors, agents and patients. It supports policy sales, premium payment, health documents, claim submission, async AI risk/fraud checks, claim review, payout and notification flows.

## Business Model

- Multiple insurance companies can own products, staff, agents, policies, claims and payments.
- Hospitals can own branches, hospital staff, doctor assignments and medical documents.
- Patients buy policies, upload/confirm medical documents and submit claims.
- Agents and insurance admins review company-scoped claims.
- Admins have global access.

## Roles

Demo password for every local user is `Test1234!`.

| Email | Role |
| --- | --- |
| `admin@saglamol.az` | `ADMIN` |
| `insurance-admin@saglamol.az` | `INSURANCE_ADMIN` |
| `insurance-staff@saglamol.az` | `INSURANCE_STAFF` |
| `agent@saglamol.az` | `AGENT` |
| `patient@saglamol.az` | `PATIENT` |
| `doctor@saglamol.az` | `DOCTOR` |
| `hospital-admin@saglamol.az` | `HOSPITAL_ADMIN` |
| `hospital-staff@saglamol.az` | `HOSPITAL_STAFF` |

Demo data is seeded only through the Liquibase `local` context. The local Docker compose file passes `LIQUIBASE_CONTEXTS=local`; do not enable that variable in production.

## Modules

| Module | Responsibility |
| --- | --- |
| `common/common-events` | Shared event contracts |
| `common/common-kafka` | Transactional outbox and idempotent consumers |
| `common/common-security` | Auth context, role checks and correlation headers |
| `common/common-exception` | Shared error model |
| `infrastructure/api-gateway` | Edge routing and JWT propagation |
| `infrastructure/config-server` | Centralized local service config |
| `infrastructure/discovery-server` | Eureka registry |
| `services/iam-service` | Users, roles, login, refresh, logout |
| `services/user-profile-service` | Companies, hospitals, branches, staff and profiles |
| `services/policy-service` | Products, coverage, policies, eligibility and reservations |
| `services/claim-service` | Claim lifecycle, review and async status updates |
| `services/health-record-service` | Health records, treatments and MinIO documents |
| `services/ai-risk-service` | External AI risk assessment with fallback |
| `services/fraud-detection-service` | Rule-based fraud scoring |
| `services/payment-service` | Premium payments and claim payouts |
| `services/notification-service` | Templates, mock senders and event notifications |

## Ports

| Component | Port |
| --- | --- |
| API Gateway | `8080` |
| IAM | `8081` |
| User Profile | `8082` |
| Policy | `8083` |
| Claim | `8084` |
| Health Record | `8085` |
| AI Risk | `8086` |
| Fraud Detection | `8087` |
| Notification | `8088` |
| Payment | `8089` |
| Prometheus | `9090` |
| Grafana | `3000` |
| MinIO API / Console | `9000` / `9001` |
| Eureka / Config Server | `8761` / `8888` |

## Databases

The local compose stack creates isolated PostgreSQL databases: `iam_db`, `user_db`, `policy_db`, `claim_db`, `health_record_db`, `ai_analysis_db`, `fraud_db`, `notification_db`, `payment_db`.

Shared runtime tables:
- `outbox_events`
- `processed_events`
- service-owned domain tables

## Environment

Copy defaults:

```powershell
Copy-Item .env.example .env
```

Important variables:

| Variable | Purpose |
| --- | --- |
| `INTERNAL_SERVICE_SECRET` | Internal API protection |
| `IAM_JWT_SECRET` | Local JWT signing secret |
| `LIQUIBASE_CONTEXTS` | Use `local` for demo seed only |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka endpoint |
| `MINIO_ENDPOINT` | Medical document object storage |
| `AI_PROVIDER_BASE_URL` | External AI provider endpoint |
| `AI_API_KEY` | External AI provider API key |
| `AI_MODEL_NAME` | AI model name |
| `AI_ENABLED` | Enables external AI, fallback when false |

Google Vertex AI can be used if you expose a compatible endpoint through `AI_PROVIDER_BASE_URL` and set `AI_MODEL_NAME`/`AI_API_KEY`. The project is not hard-bound to ChatGPT keys.

## Lightweight Local Startup

For weaker laptops, avoid starting the full stack at once. Build first:

```powershell
.\gradlew.bat clean build
```

Start infrastructure first:

```powershell
docker compose up -d postgres redis zookeeper kafka minio discovery-server config-server
```

Then start only the service group you need:

```powershell
docker compose up -d iam-service user-profile-service
docker compose up -d policy-service payment-service
docker compose up -d claim-service fraud-detection-service ai-risk-service notification-service
docker compose up -d health-record-service
```

Full startup is available but heavier:

```powershell
docker compose up -d --build
```

Reset local demo data:

```powershell
docker compose down --volumes --remove-orphans
```

## Demo Flow

1. Login as `admin@saglamol.az`.
2. Inspect seeded company, hospital, branch and demo users.
3. Login as `patient@saglamol.az`.
4. Use seeded active policy `DEMO-POL-0001`.
5. Create health record and confirm a medical document.
6. Create claim, add item with document id, submit claim.
7. AI Risk and Fraud consume `ClaimSubmittedEvent` asynchronously.
8. Login as `agent@saglamol.az`.
9. Start review and approve/reject the claim.
10. Payment creates payout from `ClaimApprovedEvent`.
11. Notification service creates local mock notifications.

## Swagger

Every business service exposes:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/api-docs`

Examples:

| Service | URL |
| --- | --- |
| IAM | `http://localhost:8081/swagger-ui.html` |
| User Profile | `http://localhost:8082/swagger-ui.html` |
| Policy | `http://localhost:8083/swagger-ui.html` |
| Claim | `http://localhost:8084/swagger-ui.html` |
| Health Record | `http://localhost:8085/swagger-ui.html` |
| AI Risk | `http://localhost:8086/swagger-ui.html` |
| Fraud | `http://localhost:8087/swagger-ui.html` |
| Notification | `http://localhost:8088/swagger-ui.html` |
| Payment | `http://localhost:8089/swagger-ui.html` |

Postman collection: [docs/postman_collection.json](docs/postman_collection.json)

## Kafka Topics

- `profile.events`
- `policy.events`
- `claim.events`
- `health-record.events`
- `risk.events`
- `fraud.events`
- `payment.events`
- `notification.events`
- DLT topics use the `.dlt` suffix.

## Observability

All services expose:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

Prometheus: `http://localhost:9090`

Grafana: `http://localhost:3000`

Default Grafana credentials are `admin/admin` unless changed in `.env`.

Logs include `X-Correlation-Id` through MDC as `correlationId`. Do not log passwords, tokens, API keys, medical file payloads or raw provider secrets.

## MinIO

MinIO stores medical documents. Local defaults:

- API: `http://localhost:9000`
- Console: `http://localhost:9001`
- User/password: `minioadmin/minioadmin`

## Testing

Unit and focused integration tests:

```powershell
.\gradlew.bat test
```

Module-level examples:

```powershell
.\gradlew.bat :services:iam-service:test
.\gradlew.bat :services:policy-service:test
.\gradlew.bat :services:claim-service:test
```

Docker smoke test, when the laptop can handle it:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1 -ResetVolumes -HealthRetries 60 -HealthDelaySeconds 15
```

See [docs/testing.md](docs/testing.md).

## Documentation

- [docs/architecture.md](docs/architecture.md)
- [docs/event-flow-p21.md](docs/event-flow-p21.md)
- [docs/claim-lifecycle.md](docs/claim-lifecycle.md)
- [docs/health-record-p17-minio-api.md](docs/health-record-p17-minio-api.md)
- [docs/fraud-p18-scoring.md](docs/fraud-p18-scoring.md)
- [docs/document-hash-cross-check-p22.md](docs/document-hash-cross-check-p22.md)

## Known Limitations

- Real payment provider integration is not included yet; payment is mock/local.
- Real SMS/email providers are not included yet; notification senders are mock/local.
- AI integration uses a simple API key against a compatible provider endpoint or fallback mode.
- OCR and FHIR/HL7 interoperability are not implemented.
- Kubernetes manifests are not included yet.
- Production secrets management is not implemented yet; use environment variables and external secret storage in deployment.
- Full load testing and independent security audit are future-phase work.
- Full-stack Docker startup can be heavy on low-resource laptops; prefer staged service groups.
- Demo seed data is for local development only.

## Roadmap

- Dedicated Grafana dashboard provisioning.
- Stronger end-to-end seeded Postman assertions.
- Broker-level DLT integration tests.
- Production-grade secret management.
- Real payment and notification providers.
