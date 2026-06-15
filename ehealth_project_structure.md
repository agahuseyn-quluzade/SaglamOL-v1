# AI E-health Insurance Backend — Codex / Claude Code Project Structure

> Bu sənəd Codex AI alətinə proyekti hazırlatmaq üçün tam struktur, modul siyahısı,
> hər servis üçün package layout, DB schema, event contract və başlanğıc prompt-ları ehtiva edir.

---

## 1. Repo Layout (Gradle Multi-Module Monorepo)

```
ehealth-insurance/
│
├── build.gradle                          # root build
├── settings.gradle                       # includes all modules
├── gradle.properties
├── docker-compose.yml                    # local MVP stack
├── docker-compose.monitoring.yml         # prometheus + grafana + loki
│
├── common/
│   ├── common-events/                    # shared Kafka event envelope + payloads
│   ├── common-security/                  # JWT helper, SecurityContextHolder util
│   └── common-exception/                 # base exceptions, error response DTO
│
├── infrastructure/
│   ├── api-gateway/                      # Spring Cloud Gateway
│   ├── config-server/                    # Spring Cloud Config
│   └── discovery-server/                 # Eureka Server
│
├── services/
│   ├── auth-service/
│   ├── user-profile-service/
│   ├── policy-service/
│   ├── claim-service/
│   ├── health-record-service/
│   ├── ai-risk-service/
│   ├── fraud-detection-service/
│   └── notification-service/
│
└── docs/
    ├── architecture.md
    ├── api-contracts/                    # OpenAPI YAML files per service
    └── postman/                          # Postman collection
```

---

## 2. Her Servis Üçün Standart Package Strukturu

Aşağıdakı layout **hər service** üçün eynidir.
`{service}` → `auth`, `userprofle`, `policy`, `claim`, `healthrecord`, `airisk`, `fraud`, `notification`

```
services/{service}-service/
├── src/
│   ├── main/
│   │   ├── java/com/ehealth/{service}/
│   │   │   ├── controller/          # REST controllers — sadəcə HTTP layer
│   │   │   ├── service/             # Application service / use-case orchestration
│   │   │   ├── repository/          # Spring Data JPA repositories
│   │   │   ├── entity/              # JPA entity-lər (yalnız öz DB-i)
│   │   │   ├── dto/
│   │   │   │   ├── request/         # incoming request DTO-lar
│   │   │   │   └── response/        # outgoing response DTO-lar
│   │   │   ├── mapper/              # MapStruct mapper-lər
│   │   │   ├── config/              # SecurityConfig, KafkaConfig, OpenAPIConfig
│   │   │   ├── exception/           # Domain exception-lar + @RestControllerAdvice
│   │   │   ├── security/            # Method security, role check helper
│   │   │   └── event/
│   │   │       ├── producer/        # Kafka producer + OutboxPublisher
│   │   │       └── consumer/        # Kafka consumer + idempotency check
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── db/changelog/        # Liquibase migration files
│   │           ├── db.changelog-master.xml
│   │           └── migrations/
│   │               └── V001__init.sql
│   └── test/
│       └── java/com/ehealth/{service}/
│           ├── controller/          # MockMvc / WebMvcTest
│           ├── service/             # Unit tests
│           └── integration/         # @SpringBootTest + Testcontainers
├── Dockerfile
└── build.gradle
```

---

## 3. Module-by-Module: Entity, DB, Key Classes

### 3.1 auth-service
**DB:** `auth_db`

**Entities:**
```
UserAccount       — id, email, passwordHash, status, createdAt, lastLoginAt
Role              — id, code, name
Permission        — id, code, description
UserRole          — userId, roleId
RefreshToken      — id, userId, tokenHash, familyId, issuedAt, expiresAt,
                    revokedAt, replacedByTokenId, ipAddress, userAgent
```

**Key Classes:**
```
AuthController          POST /api/v1/auth/register
                        POST /api/v1/auth/login
                        POST /api/v1/auth/refresh
                        GET  /api/v1/auth/me

AuthApplicationService  register(), login(), refreshToken(), revokeFamily()
JwtTokenProvider        generateAccessToken(), validateToken(), extractClaims()
RefreshTokenService     issue(), rotate(), revokeFamily(), detectReuse()
```

---

### 3.2 user-profile-service
**DB:** `user_db`

**Entities:**
```
PatientProfile    — id, userId, firstName, lastName, dateOfBirth, phone
DoctorProfile     — id, userId, licenseNo, hospitalId, specialty
AgentProfile      — id, userId, employeeNo, department
Address           — id, ownerId, ownerType, city, line1
```

**Key Classes:**
```
UserProfileController   POST /api/v1/users/patients
                        GET  /api/v1/users/patients/{id}
                        PUT  /api/v1/users/patients/{id}
                        (doctor, agent endpoints analoji)

ProfileApplicationService  createPatient(), getPatient(), updatePatient()
```

---

### 3.3 policy-service
**DB:** `policy_db`

**Entities:**
```
InsurancePlan         — id, code, name, status, monthlyPrice
CoverageRule          — id, planId, serviceType, coveragePercent,
                        annualLimit, waitingPeriodDays
Policy                — id, patientId, planId, status, startDate, endDate
PolicyLimitUsage      — id, policyId, serviceType, annualLimit,
                        usedAmount, reservedAmount, version  ← optimistic lock
PolicyLimitReservation — id, policyId, claimId, serviceType,
                        reservedAmount, status, createdAt, updatedAt
```

**Key Classes:**
```
PolicyController        POST /api/v1/policies/plans
                        POST /api/v1/policies
                        POST /api/v1/policies/eligibility-check
                        GET  /api/v1/policies/{id}

PolicyApplicationService  createPlan(), issuePolicy(), checkEligibility()
CoverageRuleEngine        evaluate(policy, serviceType, amount)
EligibilityEvaluator      isEligible(policyId, serviceType)
CoverageCacheAdapter      Redis cache wrapper
PaymentEventConsumer      PaymentCompletedEvent → consume etmek
PolicyEventProducer       PolicyCreatedEvent, PolicyActivatedEvent → outbox
```

---

### 3.4 claim-service  ← Ən mürəkkəb servis
**DB:** `claim_db`

**Entities:**
```
Claim               — id, patientId, policyId, status, claimType,
                      totalAmount, createdAt
ClaimItem           — id, claimId, serviceType, amount, serviceDate
ClaimDecision       — id, claimId, decision, decidedBy, reason
ClaimStatusHistory  — id, claimId, oldStatus, newStatus, changedAt
OutboxEvent         — id, aggregateType, aggregateId, eventType,
                      schemaVersion, payload, status, retryCount,
                      nextRetryAt, lastError, createdAt, publishedAt
ConsumerProcessedEvent — eventId, consumerName, processedAt
```

**Claim Status Machine:**
```
DRAFT → SUBMITTED → AI_PENDING_RETRY | FRAUD_CHECK_PENDING_RETRY
     → MANUAL_REVIEW_REQUIRED → APPROVED / REJECTED / NEED_MORE_INFO
     → PAYMENT_FAILED | DOCUMENT_REQUIRED | ESCALATED
```

**Key Classes:**
```
ClaimController         POST /api/v1/claims
                        PUT  /api/v1/claims/{id}/submit
                        PUT  /api/v1/claims/{id}/approve
                        PUT  /api/v1/claims/{id}/reject
                        GET  /api/v1/claims/{id}

ClaimApplicationService   submitClaim(), approveClaim(), rejectClaim()
ClaimStatusManager        transition(claimId, newStatus) — validates state machine
ClaimDecisionService      recordDecision(claimId, decision, agentId, reason)
EligibilityClient         REST call → policy-service /eligibility-check
RiskClient                REST call → ai-risk-service /claims/{id}/score
HealthRecordClient        REST call → health-record-service /records/{id}
ClaimEventProducer        ClaimSubmittedEvent → outbox
ClaimEventConsumer        RiskScoredEvent, FraudCheckedEvent → idempotent consume
OutboxPublisher           @Scheduled poll → Kafka publish
```

---

### 3.5 health-record-service
**DB:** `health_record_db`  +  **Object Storage:** MinIO/S3

**Entities:**
```
HealthRecord      — id, patientId, doctorId, diagnosisCode, treatmentDate
Treatment         — id, healthRecordId, serviceType, description
MedicalDocument   — id, healthRecordId, fileName, storagePath, sha256Hash,
                    fileSizeBytes, mimeType, status  ← PENDING_UPLOAD | CONFIRMED
                                                        | UPLOAD_FAILED | ORPHAN_CANDIDATE
HealthAccessLog   — id, recordId, actorId, action, createdAt
DocumentHashIndex — id, documentId, sha256Hash, fileSizeBytes, mimeType,
                    invoiceNumber, clinicId, patientId, firstSeenAt
```

**Key Classes:**
```
HealthRecordController  POST /api/v1/health-records
                        GET  /api/v1/health-records/{id}
                        POST /api/v1/health-records/{id}/documents

HealthRecordApplicationService  createRecord(), linkDocument()
DocumentUploadService           uploadToS3(file) → PENDING → CONFIRMED/UPLOAD_FAILED
ObjectStorageAdapter            MinIO/S3 abstraction
HealthRecordEventProducer       HealthRecordCreatedEvent, DocumentConfirmedEvent → outbox
```

---

### 3.6 ai-risk-service
**DB:** `ai_analysis_db`

**Entities:**
```
RiskAnalysis    — id, claimId, riskScore, riskLevel, confidence, modelVersion
RiskReason      — id, analysisId, reasonCode, description
AiRequestLog    — id, claimId, provider, latencyMs, status
```

**Key Classes:**
```
RiskAnalysisController  POST /api/v1/ai-risk/claims/{claimId}/score
                        GET  /api/v1/ai-risk/claims/{claimId}

RiskAnalysisApplicationService  analyzeRisk(claimId)
FeatureBuilder                  buildFeatures(claimData, policyContext, healthData)
RiskModelClient                 callExternalLLM(features) — with 10s timeout + 3 retry
ExplanationBuilder              buildReasons(modelOutput) → riskLevel, confidence, reasons[]
PolicyContextClient             REST → policy-service coverage context
ClaimEventConsumer              ClaimSubmittedEvent → trigger analysis
RiskEventProducer               RiskScoredEvent → outbox
```

**AI Fallback Logic:**
```
timeout (10s) → retry 1min → retry 5min → retry 15min
→ maxRetry aşıldı → MANUAL_REVIEW_REQUIRED → AiRiskAnalysisFailedEvent
```

---

### 3.7 fraud-detection-service
**DB:** `fraud_db`

**Entities:**
```
FraudCheck        — id, claimId, fraudScore, status, checkedAt
FraudSignal       — id, fraudCheckId, signalType, severity, description
DocumentHashIndex — id, sha256Hash, fileSizeBytes, mimeType,
                    invoiceNumber, clinicId, patientId, firstSeenAt
```

**Key Classes:**
```
FraudController         POST /api/v1/fraud/claims/{claimId}/check
                        GET  /api/v1/fraud/claims/{claimId}

FraudDetectionApplicationService  checkFraud(claimId)
RuleEngine                duplicate, limit abuse, suspicious timing rules
DocumentHashMatcher       SHA-256 hash + metadata comparison
AnomalyDetector           unusual amount/pattern detection
ClaimHistoryClient        REST → claim-service previous claims context
ClaimEventConsumer        ClaimSubmitted/RiskScored → idempotent consume
FraudEventProducer        FraudCheckedEvent | FraudFlaggedEvent → outbox
```

---

### 3.8 notification-service
**DB:** `notification_db`

**Entities:**
```
Notification          — id, recipientUserId, channel, status, subject, createdAt
NotificationTemplate  — id, code, channel, body
NotificationRetry     — id, notificationId, retryCount, nextRetryAt
```

**Key Classes:**
```
NotificationController  POST /api/v1/notifications/send
                        GET  /api/v1/notifications/users/{userId}

NotificationApplicationService  send(userId, templateCode, params)
TemplateEngine                  render(templateCode, params)
EmailProvider                   send via SMTP/sandbox (MVP)
SmsProvider                     mock/console log (MVP)
RetryScheduler                  @Scheduled retry failed notifications
DomainEventConsumer             ClaimApprovedEvent, ClaimRejectedEvent,
                                MoreInfoRequestedEvent, PolicyActivatedEvent
```

---

## 4. Common Modules

### common-events
```java
// Event Envelope — bütün Kafka event-lər bu wrapper ilə göndərilir
EventEnvelope {
    String eventId;          // UUID
    String eventType;        // "ClaimSubmittedEvent"
    String schemaVersion;    // "1.0.0"
    String aggregateType;    // "CLAIM"
    String aggregateId;      // UUID
    Instant occurredAt;
    String producer;         // "claim-service"
    String correlationId;    // UUID
    Object payload;          // typed payload
}

// Payload-lar:
ClaimSubmittedEvent    { claimId, patientId, policyId, requestedAmount, currency }
RiskScoredEvent        { claimId, riskScore, riskLevel, confidence, reasons[] }
FraudCheckedEvent      { claimId, fraudScore, status, signals[] }
FraudFlaggedEvent      { claimId, fraudScore, signals[] }
ClaimApprovedEvent     { claimId, patientId, approvedAmount }
ClaimRejectedEvent     { claimId, patientId, reason }
MoreInfoRequestedEvent { claimId, patientId, requestedDocs[] }
PolicyCreatedEvent     { policyId, patientId, planId }
PolicyActivatedEvent   { policyId, patientId }
HealthRecordCreatedEvent { recordId, patientId, doctorId }
DocumentConfirmedEvent   { documentId, healthRecordId, sha256Hash }
PolicyLimitReleasedEvent { policyId, claimId, releasedAmount }
AiRiskAnalysisFailedEvent { claimId, reason }
FraudCheckFailedEvent     { claimId, reason }
DocumentUploadFailedEvent { documentId, healthRecordId }
ClaimPayoutFailedEvent    { claimId, paymentProvider, reason }
```

### common-security
```java
JwtValidationFilter     // API Gateway-də istifadə: JWT parse + X-User-Id, X-User-Roles header
TrustedContextFilter    // Internal service-lərdə: header-dən SecurityContext qur
RoleConstants           // PATIENT, DOCTOR, AGENT, ADMIN, SYSTEM
SecurityContextHelper   // getCurrentUserId(), hasRole()
```

### common-exception
```java
ErrorResponse { timestamp, status, errorCode, message, path, correlationId }
BaseBusinessException
ResourceNotFoundException
InvalidStatusTransitionException
PolicyNotEligibleException
DuplicateDocumentException
```

---

## 5. Infrastructure

### api-gateway/application.yml (əsas routing)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates: [Path=/api/v1/auth/**]
        - id: user-profile-service
          uri: lb://user-profile-service
          predicates: [Path=/api/v1/users/**]
        - id: policy-service
          uri: lb://policy-service
          predicates: [Path=/api/v1/policies/**]
        - id: claim-service
          uri: lb://claim-service
          predicates: [Path=/api/v1/claims/**]
        - id: health-record-service
          uri: lb://health-record-service
          predicates: [Path=/api/v1/health-records/**]
        - id: ai-risk-service
          uri: lb://ai-risk-service
          predicates: [Path=/api/v1/ai-risk/**]
        - id: fraud-detection-service
          uri: lb://fraud-detection-service
          predicates: [Path=/api/v1/fraud/**]
        - id: notification-service
          uri: lb://notification-service
          predicates: [Path=/api/v1/notifications/**]

      default-filters:
        - JwtValidationFilter
        - RequestCorrelationFilter
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 20
            redis-rate-limiter.burstCapacity: 40
```

### docker-compose.yml (MVP local stack)
```yaml
services:
  postgres-auth:       image: postgres:16, port: 5432, db: auth_db
  postgres-user:       image: postgres:16, port: 5433, db: user_db
  postgres-policy:     image: postgres:16, port: 5434, db: policy_db
  postgres-claim:      image: postgres:16, port: 5435, db: claim_db
  postgres-health:     image: postgres:16, port: 5436, db: health_record_db
  postgres-ai:         image: postgres:16, port: 5437, db: ai_analysis_db
  postgres-fraud:      image: postgres:16, port: 5438, db: fraud_db
  postgres-notify:     image: postgres:16, port: 5439, db: notification_db

  redis:               image: redis:7-alpine, port: 6379
  kafka:               image: confluentinc/cp-kafka:7.6.0, port: 9092
  zookeeper:           image: confluentinc/cp-zookeeper:7.6.0
  minio:               image: minio/minio, port: 9000/9001

  config-server:       port: 8888
  discovery-server:    port: 8761
  api-gateway:         port: 8080

  auth-service:        port: 8081
  user-profile-service: port: 8082
  policy-service:      port: 8083
  claim-service:       port: 8084
  health-record-service: port: 8085
  ai-risk-service:     port: 8086
  fraud-detection-service: port: 8087
  notification-service: port: 8088
```

---

## 6. Kafka Topics

```
Topic                           Producers               Consumers
─────────────────────────────────────────────────────────────────────────
claim.submitted                 claim-service           ai-risk-service
                                                        fraud-detection-service
                                                        notification-service
claim.risk.scored               ai-risk-service         claim-service
claim.fraud.checked             fraud-detection-service claim-service
claim.approved                  claim-service           notification-service
                                                        policy-service (limit update)
claim.rejected                  claim-service           notification-service
                                                        policy-service (limit release)
claim.more-info-requested       claim-service           notification-service
policy.created                  policy-service          notification-service
policy.activated                policy-service          notification-service
health-record.created           health-record-service   (future: AI document analysis)
document.confirmed              health-record-service   claim-service
ai-risk.failed                  ai-risk-service         claim-service
fraud-check.failed              fraud-detection-service claim-service
policy-limit.released           policy-service          claim-service
```

---

## 7. Codex AI Üçün Başlanğıc Prompt-lar

### Prompt 1 — Repo Skeleton Yarat
```
Create a Gradle multi-module Spring Boot monorepo for an e-health insurance backend.

Modules:
- common/common-events
- common/common-security
- common/common-exception
- infrastructure/api-gateway (Spring Cloud Gateway)
- infrastructure/config-server (Spring Cloud Config)
- infrastructure/discovery-server (Eureka)
- services/auth-service
- services/user-profile-service
- services/policy-service
- services/claim-service
- services/health-record-service
- services/ai-risk-service
- services/fraud-detection-service
- services/notification-service

Root build.gradle should use Java 21, Spring Boot 3.3.x, Spring Cloud 2023.x.
Each service should have its own build.gradle with dependencies:
spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security,
spring-boot-starter-validation, spring-boot-starter-actuator,
spring-kafka, liquibase-core, postgresql driver, mapstruct, lombok.

Generate settings.gradle, root build.gradle, and skeleton build.gradle for each module.
```

---

### Prompt 2 — Auth Service
```
Implement auth-service for an e-health insurance platform using Java 21 + Spring Boot 3.3.

Entities in auth_db (PostgreSQL):
- user_account (id UUID, email, password_hash, status ENUM[ACTIVE,INACTIVE,LOCKED], created_at, last_login_at)
- role (id UUID, code, name)
- permission (id UUID, code, description)
- user_role (user_id, role_id)
- refresh_token (id UUID, user_id, token_hash VARCHAR(255), family_id UUID,
  issued_at, expires_at, revoked_at, replaced_by_token_id, ip_address, user_agent)

REST endpoints:
- POST /api/v1/auth/register → 201 { userId, status }
- POST /api/v1/auth/login    → 200 { accessToken, refreshToken } | 401
- POST /api/v1/auth/refresh  → 200 { accessToken } | 401
- GET  /api/v1/auth/me       → 200 { userId, roles[] }

Business rules:
- Password must be bcrypt hashed (never plaintext)
- JWT access token: 15 min expiry, signed with RSA private key
- Refresh token: one-time use, rotation on each use
- If a revoked refresh token is reused → revoke entire family_id (token reuse attack)
- Roles: PATIENT, DOCTOR, AGENT, ADMIN, SYSTEM

Use Liquibase for DB migrations.
Use @RestControllerAdvice for error handling. Return ErrorResponse { timestamp, status, errorCode, message, path, correlationId }.
Publish UserRegisteredEvent to Kafka via transactional outbox pattern.
```

---

### Prompt 3 — Claim Service (Outbox + Saga)
```
Implement claim-service for an e-health insurance platform using Java 21 + Spring Boot 3.3.

Entities in claim_db:
- claim (id UUID, patient_id, policy_id, status VARCHAR(50), claim_type, total_amount, created_at)
- claim_item (id, claim_id, service_type, amount, service_date)
- claim_decision (id, claim_id, decision, decided_by, reason)
- claim_status_history (id, claim_id, old_status, new_status, changed_at)
- outbox_event (id UUID, aggregate_type, aggregate_id, event_type, schema_version,
                payload JSONB, status DEFAULT 'NEW', retry_count INT DEFAULT 0,
                next_retry_at TIMESTAMP, last_error TEXT, created_at, published_at)
- consumer_processed_event (event_id UUID, consumer_name VARCHAR(120), processed_at) PK(event_id, consumer_name)

Claim status machine:
DRAFT → SUBMITTED → AI_PENDING_RETRY | FRAUD_CHECK_PENDING_RETRY
→ MANUAL_REVIEW_REQUIRED → APPROVED | REJECTED | NEED_MORE_INFO
→ PAYMENT_FAILED | DOCUMENT_REQUIRED | ESCALATED

REST endpoints:
- POST   /api/v1/claims              → 201 DRAFT
- PUT    /api/v1/claims/{id}/submit  → 200 SUBMITTED | 409 invalid transition
- PUT    /api/v1/claims/{id}/approve → 200 APPROVED  | 409 (AGENT only)
- PUT    /api/v1/claims/{id}/reject  → 200 REJECTED  | 409 (AGENT only)
- GET    /api/v1/claims/{id}

Business rules:
- On submit: call policy-service /eligibility-check synchronously. If not eligible → reject.
- On submit: write ClaimSubmittedEvent to outbox_event in the SAME DB transaction as claim row.
- OutboxPublisher: @Scheduled every 5s, poll status=NEW or FAILED with next_retry_at<=now, publish to Kafka topic "claim.submitted", mark PUBLISHED.
- On publish fail: increment retry_count, set next_retry_at with exponential backoff (1min, 5min, 15min).
- Consume RiskScoredEvent and FraudCheckedEvent idempotently (check consumer_processed_event before processing).
- On AI/Fraud service timeout (no event after 30s): move claim to MANUAL_REVIEW_REQUIRED.

Feign clients:
- EligibilityClient → policy-service
- RiskClient        → ai-risk-service
- HealthRecordClient → health-record-service

Use ClaimStatusManager to validate all state transitions and throw InvalidStatusTransitionException on invalid.
```

---

### Prompt 4 — Policy Service (Race Condition Fix)
```
Implement policy-service using Java 21 + Spring Boot 3.3.

Entities in policy_db:
- insurance_plan (id, code, name, status, monthly_price)
- coverage_rule (id, plan_id, service_type, coverage_percent, annual_limit, waiting_period_days)
- policy (id, patient_id, plan_id, status, start_date, end_date)
- policy_limit_usage (id, policy_id, service_type, annual_limit, used_amount DECIMAL,
                      reserved_amount DECIMAL, version BIGINT) — @Version for optimistic locking
- policy_limit_reservation (id, policy_id, claim_id, service_type, reserved_amount,
                             status ENUM[RESERVED,RELEASED,CONSUMED], created_at, updated_at)

REST endpoints:
- POST /api/v1/policies/plans              → 201 (ADMIN only)
- POST /api/v1/policies                    → 201 (AGENT/ADMIN)
- POST /api/v1/policies/eligibility-check  → 200 { eligible, coveragePercent, annualLimit, usedAmount }
- GET  /api/v1/policies/{id}

Business rules:
- Eligibility check: policy must be ACTIVE, serviceType must exist in coverage_rule,
  waiting_period must have passed, annual_limit not exhausted.
- Use Redis cache (CoverageCacheAdapter) for coverage rules with 5min TTL.
- On ClaimApprovedEvent: CONSUME reservation → move reserved_amount to used_amount.
- On ClaimRejectedEvent: RELEASE reservation → reduce reserved_amount.
- policy_limit_usage uses @Version (optimistic locking); on OptimisticLockException → retry 3 times.
- CoverageRuleEngine must check exclusions and waiting period before returning eligible=true.

Publish PolicyCreatedEvent and PolicyActivatedEvent via transactional outbox.
Consume PaymentCompletedEvent to finalize policy activation.
```

---

### Prompt 5 — Health Record Service (S3 Atomicity)
```
Implement health-record-service using Java 21 + Spring Boot 3.3 + MinIO SDK.

Entities in health_record_db:
- health_record (id, patient_id, doctor_id, diagnosis_code, treatment_date)
- treatment (id, health_record_id, service_type, description)
- medical_document (id, health_record_id, file_name, storage_path, sha256_hash,
                    file_size_bytes, mime_type,
                    status ENUM[PENDING_UPLOAD, CONFIRMED, UPLOAD_FAILED, ORPHAN_CANDIDATE])
- health_access_log (id, record_id, actor_id, action, created_at)
- document_hash_index (id, document_id, sha256_hash CHAR(64), file_size_bytes,
                       mime_type, invoice_number, clinic_id, patient_id, first_seen_at)

REST endpoints:
- POST /api/v1/health-records                    (DOCTOR/HOSPITAL)
- GET  /api/v1/health-records/{id}               (DOCTOR/AGENT/ADMIN)
- POST /api/v1/health-records/{id}/documents     (multipart, DOCTOR/HOSPITAL)

Upload atomicity — two-phase model:
1. Insert medical_document with status=PENDING_UPLOAD in DB transaction.
2. Upload file to MinIO/S3.
3. If upload succeeds: compute SHA-256, update status=CONFIRMED, publish DocumentConfirmedEvent via outbox.
4. If upload fails: update status=UPLOAD_FAILED. Do NOT rollback the metadata row.
5. @Scheduled cleanup job: find PENDING_UPLOAD rows older than 1 hour → mark ORPHAN_CANDIDATE.

Document hash:
- Compute SHA-256 of file content.
- Before confirming: check document_hash_index. If same hash + same file_size + invoice_number found → flag as potential duplicate → FraudCheckNeededEvent.
- Insert into document_hash_index after CONFIRMED.

ObjectStorageAdapter should be an interface with MinioObjectStorageAdapter implementation.
Access is logged in health_access_log for every read operation.
```

---

### Prompt 6 — AI Risk Service (Fallback + Retry)
```
Implement ai-risk-service using Java 21 + Spring Boot 3.3.

Entities in ai_analysis_db:
- risk_analysis (id, claim_id, riskScore INT, riskLevel ENUM[LOW,MEDIUM,HIGH],
                 confidence DECIMAL, model_version, created_at)
- risk_reason (id, analysis_id, reason_code, description)
- ai_request_log (id, claim_id, provider, latency_ms, status, created_at)

REST endpoints:
- POST /api/v1/ai-risk/claims/{claimId}/score  (CLAIM_SERVICE/AGENT)
- GET  /api/v1/ai-risk/claims/{claimId}        (AGENT/ADMIN)

Business rules:
- Consume ClaimSubmittedEvent from Kafka (idempotent).
- FeatureBuilder: fetch claim data, call PolicyContextClient (policy-service), call HealthData if needed.
- Call RiskModelClient (external LLM/ML API) with 10s timeout.
- On timeout: retry with backoff (1min, 5min, 15min). Max 3 retries.
- If all retries fail: publish AiRiskAnalysisFailedEvent via outbox.
- ExplanationBuilder: translate model output into { riskScore: 0-100, riskLevel, confidence: 0-1, reasons[] }.
- RiskModelClient must be behind interface for easy mocking (MVP uses rule-based mock).
- Publish RiskScoredEvent via transactional outbox after successful analysis.
- Log every external AI call in ai_request_log with latency.
```

---

### Prompt 7 — Fraud Detection Service
```
Implement fraud-detection-service using Java 21 + Spring Boot 3.3.

Entities in fraud_db:
- fraud_check (id, claim_id, fraud_score INT, status ENUM[CLEAR,FLAGGED,MANUAL_REVIEW], checked_at)
- fraud_signal (id, fraud_check_id, signal_type, severity ENUM[LOW,MEDIUM,HIGH], description)
- document_hash_index (id, sha256_hash CHAR(64), file_size_bytes, mime_type,
                       invoice_number, clinic_id, patient_id, first_seen_at)

REST endpoints:
- POST /api/v1/fraud/claims/{claimId}/check  (CLAIM_SERVICE/AGENT)
- GET  /api/v1/fraud/claims/{claimId}        (AGENT/ADMIN)

Business rules:
- Consume ClaimSubmitted/RiskScored events (idempotent, check consumer_processed_event).
- RuleEngine checks:
    1. DUPLICATE_DOCUMENT: SHA-256 hash + file_size match in document_hash_index
    2. SUSPICIOUS_TIMING: same patient, same service_type within 7 days
    3. COVERAGE_ABUSE: claim amount > 2x average for this service_type
    4. FREQUENT_CLAIMS: more than 5 claims in 30 days for same patient
- AnomalyDetector: statistical threshold on claim amounts (mean + 2*stddev).
- If fraudScore >= 70 → status=FLAGGED → publish FraudFlaggedEvent
- If fraudScore < 70  → status=CLEAR  → publish FraudCheckedEvent
- On service failure  → publish FraudCheckFailedEvent
- ClaimHistoryClient: Feign → claim-service /claims?patientId={id}&from={date}
```

---

### Prompt 8 — docker-compose.yml (Tam MVP Stack)
```
Generate a complete docker-compose.yml for the e-health insurance backend MVP.

Services needed:
1. Eight PostgreSQL 16 instances (one per service), each with unique port (5432-5439) and database name.
2. Redis 7 alpine (port 6379) — for rate limiting and coverage cache.
3. Apache Kafka + Zookeeper (Confluent 7.6) — port 9092.
4. MinIO (port 9000 API, 9001 console) — for document storage.
5. All 8 microservices with proper depends_on, SPRING_DATASOURCE_URL, KAFKA_BOOTSTRAP_SERVERS env vars.
6. api-gateway (port 8080), config-server (port 8888), discovery-server (port 8761).
7. Prometheus (port 9090) + Grafana (port 3000) for monitoring.

Each service should have:
- healthcheck with /actuator/health
- restart: unless-stopped
- network: ehealth-network

Use environment variables for all credentials. Secrets must NOT be hardcoded — use .env file reference.
```

---

## 8. Sprint → Delivery Checklist

### Week 1 ✅
- [ ] Monorepo skeleton + build passes
- [ ] Docker Compose ayağa qalxır (DB-lər, Kafka, MinIO, Redis)
- [ ] Config Server + Discovery Server işləyir
- [ ] API Gateway route edir
- [ ] Hər servisin `/actuator/health` işləyir
- [ ] Liquibase migration-lar işləyir

### Week 2 ✅
- [ ] Auth: register/login/refresh/me işləyir, JWT validate olunur
- [ ] Policy: plan CRUD, policy issue, eligibility check işləyir
- [ ] Claim: create/submit/approve/reject lifecycle işləyir
- [ ] Health Record: create + document upload (PENDING→CONFIRMED) işləyir

### Week 3 ✅
- [ ] Kafka topics yaradılır
- [ ] ClaimSubmittedEvent → AI Risk + Fraud servislərə çatır
- [ ] RiskScoredEvent + FraudCheckedEvent → Claim Service-ə qayıdır
- [ ] Outbox publisher işləyir (DB → Kafka)
- [ ] Idempotency: consumer_processed_event yoxlanılır
- [ ] AI timeout → MANUAL_REVIEW_REQUIRED işləyir

### Week 4 ✅
- [ ] E2E happy path test keçir (Postman collection)
- [ ] Error path test: AI fail, document fail, invalid transition
- [ ] Grafana dashboard metrics göstərir
- [ ] Swagger docs bütün servislər üçün açılır
- [ ] Repo-da plaintext secret yoxdur
- [ ] Demo script + seed data hazırdır
