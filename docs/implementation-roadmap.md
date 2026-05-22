# SaglamOL Implementation Roadmap

Bu roadmap layihəni mərhələli qurmaq üçündür. Əsas prinsip: əvvəl işləyən
REST əsaslı MVP, sonra event-driven hardening.

## Phase 1 - Foundation

- Gradle multi-module monorepo
- Common modules
- Infrastructure modules
- Service skeleton-ları
- Java 21 + Spring Boot 3.x bazası
- Health endpoint-lər üçün minimal app-lər
- Docker Compose infra stack:
  PostgreSQL, Redis, Kafka/Zookeeper, MinIO

## Phase 2 - IAM And Gateway

- Register/login/refresh
- JWT access token
- Refresh token rotation
- Roles and permissions
- API Gateway routing
- Gateway-də JWT validation
- Internal trust headers

## Phase 3 - Profiles And Policies

- Patient, doctor, agent profile
- Insurance plan
- Coverage rule
- Policy issue
- Eligibility check

## Phase 4 - Claim Core

- Claim create/submit
- Claim status machine
- Approve/reject
- Policy eligibility integration
- Claim status history

## Phase 5 - Health Records

- Health record create/read
- Treatment data
- Medical document metadata
- MinIO upload
- `PENDING_UPLOAD -> CONFIRMED/UPLOAD_FAILED`

## Phase 6 - Simple AI Risk

- API-key based AI provider config
- `RiskModelClient` interface
- Timeout and simple retry
- Risk score, level, confidence, reasons
- AI request logging for future billing

## Phase 7 - Fraud Detection

- Rule-based fraud scoring
- Duplicate document signal
- Suspicious timing
- Frequent claims
- High amount anomaly

## Phase 8 - Events And Outbox

- Kafka topics
- Transactional outbox
- Idempotent consumers
- Retry/backoff
- Claim submitted, risk scored, fraud checked events

## Phase 9 - Notification

- Notification templates
- Email mock
- SMS mock
- Retry scheduler
- Status-change notification events

## Phase 10 - Payment

- Premium payment tracking
- Payout/refund request tracking
- External payment provider adapter
- PaymentCompletedEvent
- ClaimPayoutFailedEvent

## Phase 11 - Demo, Tests, Monitoring

- Swagger/OpenAPI
- Postman collection
- E2E happy path
- Failure path tests
- Prometheus/Grafana
- Demo seed data
