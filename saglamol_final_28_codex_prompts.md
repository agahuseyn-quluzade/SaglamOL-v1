# SaglamOL — Final 28 Codex Prompt Planı

Bu fayl `saglamol_improved_28_prompts.md` planı ilə əvvəlki 24 promptlu planın müqayisəsi əsasında hazırlanmış yekun versiyadır.

## Yekun qərar

28 promptlu plan istifadə edilməlidir. Səbəblər:

- 24 promptlu planda böyük mərhələlər vardı; 28-lik versiyada onlar daha kiçik və idarəolunan mərhələlərə bölünüb.
- Outbox/Kafka infrastrukturu gec mərhələdən çıxarılıb erkən mərhələyə keçirilib.
- `InsurancePlan` → `InsuranceProduct` keçidi ayrıca migration riski kimi göstərilib.
- Regression fix və production hardening ayrı mərhələlərə bölünüb.
- User Profile Service artıq tək promptla yüklənmir; data model, ownership və final API ayrı verilir.
- Çox şirkətli SaglamOL platforma modeli qorunur: `InsuranceCompany`, `INSURANCE_ADMIN`, `INSURANCE_STAFF`, `insuranceCompanyId`, hospital-aware claim/payment/policy flow.

## İstifadə qaydası

```text
1 prompt → build/test → commit → növbəti prompt
```

Minimum yoxlama:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose config --quiet
```

---

## Prompt 1 — Pre-flight Audit

```text
SaglamOL layihəsinin hazırkı vəziyyətini analiz et və heç bir biznes kodu yazmadan texniki audit apar.

Layihə backend-only çox şirkətli E-Health Insurance microservice platformasıdır.

Hazır texniki baza:
- Java 21, Spring Boot 3.3.6, Spring Cloud 2023.0.4
- Gradle multi-module monorepo
- PostgreSQL, Liquibase, Docker Compose
- Redis, Kafka + Zookeeper, MinIO
- API Gateway, IAM/Auth Service, common-security
- User Profile Service, Hospital/provider modeli

Hazır olan əsas hissələr:
- IAM/Auth Service: register/login, email/phone login, refresh token, logout/revoke, role assignment, user status, password flows
- API Gateway: JWT validation, X-User-Id, X-User-Roles, X-Correlation-Id
- common-security: AuthContext, AuthContextHolder, RoleChecker, InternalAuthFilter, standardized 401/403, correlation id handling
- User Profile Service: patient profile, doctor profile, agent profile, hospital, hospital branch, hospital staff, doctor-hospital assignment, provider ownership authorization

Yeni biznes qərarı:
SaglamOL özü sığorta şirkəti deyil. SaglamOL çox şirkətli platformadır. Platformaya bir neçə InsuranceCompany qoşulacaq. Policy, Product, Claim və Payment-lər insuranceCompanyId ilə scope-lanmalıdır.

Audit tapşırıqları:
1. Monorepo strukturunu analiz et.
2. Hər service-in hazır vəziyyətini çıxar: entity, service layer, controller, Liquibase, test.
3. Mövcud entity-ləri yoxla — hansılar tam, hansılar skeleton.
4. Mövcud Liquibase changelog strukturunu yoxla. Flyway istifadə olunmadığını təsdiqlə.
5. API Gateway header contract uyğunluğunu yoxla: X-User-Id, X-User-Roles, X-Correlation-Id.
6. common-security istifadə vəziyyətini yoxla.
7. Hospital/provider ownership modelinin hazır vəziyyətini yoxla.
8. InsuranceCompany modelini əlavə etməzdən əvvəl hansı fayllara toxunmaq lazım olduğunu siyahıla.
9. Riskli hissələri qeyd et:
   - migration conflict
   - role duplication
   - ownership inconsistency
   - controller-də təkrarlanan auth logic
   - service-to-service direct DB access riski
   - Kafka event modelində köhnə single-company field-lər
   - policy/claim/payment-də insuranceCompanyId çatışmazlığı
10. Hələ heç bir kod yazma.

Sonda:
- hazır vəziyyət xülasəsi ver;
- hansı promptdan başlamaq lazım olduğunu təsdiqlə;
- riskləri prioritetləşdir;
- build/test üçün hansı command-ların işlədilməli olduğunu göstər;
- növbəti mərhələ üçün checklist hazırla.
```

---

## Prompt 2 — Config Server və Runtime Config

```text
SaglamOL layihəsində Config Server və runtime config standartlaşdırılmasını tamamla.

Hazır vəziyyət:
- infrastructure/config-server mövcuddur, native mode, classpath:/config.
- IAM/Auth Service artıq ayrıca işləkdir — config server-ə keçirməyə məcbur etmə.

Tapşırıqlar:

1. infrastructure/config-server/src/main/resources/config/ altında config faylları yarat:
- user-profile-service.yml
- policy-service.yml
- claim-service.yml
- payment-service.yml
- health-record-service.yml
- ai-risk-service.yml
- fraud-detection-service.yml
- notification-service.yml

2. Hər servis config-də bunlar olsun:
- server.port
- spring.application.name
- datasource URL/user/password env-dən
- liquibase changelog path
- actuator exposure: health,info,prometheus
- logging correlation id pattern: %X{correlationId:-}
- kafka config lazım olan servis-lərdə
- redis config lazım olan servis-lərdə (policy-service)
- MinIO config (health-record-service)
- AI config (ai-risk-service)
- internal service secret: ${INTERNAL_SERVICE_SECRET:dev-internal-secret}

3. Hər service-in öz application.yml faylında minimal self-config saxla:
- spring.application.name
- spring.config.import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
- profile fallback
- actuator basic config

4. .env.example yenilə: POSTGRES_USER, POSTGRES_PASSWORD, IAM_JWT_SECRET, INTERNAL_SERVICE_SECRET, REDIS_HOST, KAFKA_BOOTSTRAP_SERVERS, MINIO credentials, AI provider variables.

5. Docker Compose environment-lərini yoxla — Config Server URL, active profile, DB credentials.

6. IAM Service config-i pozma. Mövcud port-ları dəyişdirmə.

7. Flyway əlavə etmə. Liquibase config-ləri pozma.

8. docs/local-development.md faylına local startup qaydasını əlavə et.

Sonda: dəyişən faylları göstər; docker compose config --quiet keçdiyini təsdiqlə.
```

---

## Prompt 3 — common-events Multi-company Event Contract

```text
SaglamOL layihəsinin common/common-events modulunu çox şirkətli platforma modelinə uyğun tamamla.

Hazır vəziyyət:
- common-events modulunda EventEnvelope ola bilər.
- Platforma çox şirkətli modelə keçir.

Vacib qaydalar:
- Event-lər Java record olsun.
- insuranceCompanyId lazım olan yerlərdə mütləq olsun.
- hospitalId, doctorProfileId optional olsun.
- Patient üçün patientProfileId istifadə olunsun.
- userId ilə profileId qarışdırılmasın.
- EventEnvelope generic olmalıdır.
- common-events JPA entity-lərdən asılı olmamalıdır.

1. EventEnvelope<T> yarat:
Field-lər: eventId, eventType, aggregateType, aggregateId, occurredAt, correlationId, causationId (optional), producerService, version, payload.

2. KafkaTopics class əlavə et:
Topics: iam.events, profile.events, policy.events, payment.events, claim.events, health-record.events, risk.events, fraud.events, notification.events
DLT topics: hər birinin .dlt versiyası.

3. IAM events:
- UserRegisteredEvent(userId, email, phoneNumber, roles, occurredAt)
- UserRoleAssignedEvent(userId, roleName, occurredAt)
- UserStatusChangedEvent(userId, oldStatus, newStatus, occurredAt)

4. Profile/organization events:
- InsuranceCompanyCreatedEvent(companyId, name, taxId, occurredAt)
- InsuranceCompanyActivatedEvent(companyId, occurredAt)
- InsuranceCompanySuspendedEvent(companyId, reason, occurredAt)
- InsuranceCompanyStaffCreatedEvent(companyId, staffProfileId, iamUserId, roleType, occurredAt)
- AgentLinkedToCompanyEvent(agentProfileId, companyId, occurredAt)
- HospitalCreatedEvent(hospitalId, name, occurredAt)
- HospitalActivatedEvent(hospitalId, occurredAt)
- HospitalStaffCreatedEvent(hospitalId, staffProfileId, iamUserId, occurredAt)
- DoctorAssignedToHospitalEvent(doctorProfileId, hospitalId, branchId, occurredAt)

5. Policy events:
- InsuranceProductCreatedEvent(productId, companyId, productCode, name, occurredAt)
- InsuranceProductActivatedEvent(productId, companyId, occurredAt)
- PolicyCreatedEvent(policyId, policyNumber, companyId, productId, patientProfileId, agentProfileId, premiumAmount, occurredAt)
- PolicyPaymentPendingEvent(policyId, companyId, patientProfileId, premiumAmount, occurredAt)
- PolicyActivatedEvent(policyId, companyId, patientProfileId, occurredAt)
- PolicyCancelledEvent(policyId, companyId, patientProfileId, reason, occurredAt)
- PolicyLimitReservedEvent(reservationId, policyId, companyId, claimId, reservedAmount, occurredAt)
- PolicyLimitConfirmedEvent(reservationId, policyId, claimId, occurredAt)
- PolicyLimitReleasedEvent(reservationId, policyId, claimId, reason, occurredAt)

6. Payment events:
- PaymentPendingEvent(paymentId, paymentNumber, companyId, policyId, patientProfileId, amount, currency, paymentType, occurredAt)
- PaymentCompletedEvent(paymentId, paymentNumber, companyId, policyId, claimId, patientProfileId, hospitalId, amount, currency, paymentType, occurredAt)
- PaymentFailedEvent(paymentId, companyId, policyId, reason, occurredAt)
- ClaimPayoutRequestedEvent(paymentId, claimId, companyId, patientProfileId, hospitalId, amount, occurredAt)
- ClaimPayoutCompletedEvent(paymentId, claimId, companyId, paidAmount, occurredAt)
- ClaimPayoutFailedEvent(paymentId, claimId, companyId, reason, occurredAt)
- RefundCompletedEvent(paymentId, companyId, refundedAmount, occurredAt)

7. Claim events:
- ClaimSubmittedEvent(claimId, claimNumber, policyId, companyId, patientProfileId, hospitalId, doctorProfileId, serviceType, totalAmount, documentIds, occurredAt)
- ClaimNeedsMoreDocumentsEvent(claimId, companyId, patientProfileId, reason, occurredAt)
- ClaimStatusChangedEvent(claimId, companyId, oldStatus, newStatus, reason, occurredAt)
- ClaimApprovedEvent(claimId, companyId, patientProfileId, policyId, approvedAmount, decidedBy, occurredAt)
- ClaimRejectedEvent(claimId, companyId, patientProfileId, reason, decidedBy, occurredAt)
- ClaimPaidEvent(claimId, companyId, patientProfileId, paidAmount, occurredAt)

8. Health Record events:
- HealthRecordCreatedEvent(healthRecordId, patientProfileId, doctorProfileId, hospitalId, occurredAt)
- MedicalDocumentUploadedEvent(documentId, healthRecordId, patientProfileId, occurredAt)
- MedicalDocumentConfirmedEvent(documentId, healthRecordId, patientProfileId, sha256Hash, occurredAt)

9. AI Risk events:
- RiskAnalysisRequestedEvent(claimId, companyId, patientProfileId, policyId, occurredAt)
- RiskAnalysisCompletedEvent(claimId, companyId, riskAnalysisId, score, level, confidence, reasons, occurredAt)

10. Fraud events:
- FraudCheckRequestedEvent(claimId, companyId, patientProfileId, policyId, totalAmount, occurredAt)
- FraudCheckCompletedEvent(claimId, companyId, fraudCheckId, fraudScore, fraudLevel, signals, passed, occurredAt)

11. Notification events:
- NotificationRequestedEvent(recipientUserId, channel, templateCode, variables, relatedEntityType, relatedEntityId, companyId, hospitalId, occurredAt)
- NotificationSentEvent(notificationId, recipientUserId, channel, occurredAt)
- NotificationFailedEvent(notificationId, recipientUserId, channel, reason, occurredAt)

12. build.gradle: minimal dependency — Jackson annotation-lar, Java record-lar üçün lazımdırsa.

13. Testlər: EventEnvelope serialization, KafkaTopics constants, ClaimSubmittedEvent field, PaymentCompletedEvent field, PolicyCreatedEvent field.

Sonda: yaradılan event-ləri siyahıla; multi-company field-lərin harada istifadə olunduğunu izah et; test command ver.
```

---

## Prompt 4 — Outbox + Kafka Infrastructure (ƏVVƏLKİ 18 — ERKƏNƏ KÖÇÜRÜLDÜ)

```text
Bütün event-producing service-lərdə istifadə olunacaq Transactional Outbox və idempotent consumer infrastructure-u ERKƏN mərhələdə qurmaq.

Bu prompt ERKƏN icra olunur ki, sonrakı servis promptları hazır infrastructure üzərində qurulsun.

Məqsəd: Vahid, yenidən istifadə edilə bilən outbox/consumer mexanizmi yaratmaq.

1. common moduluna yeni modul əlavə et: common/common-kafka

settings.gradle-a əlavə et: include 'common:common-kafka'

common-kafka/build.gradle:
- java-library plugin
- spring-kafka dependency
- spring-boot-starter-data-jpa dependency (compile only)
- jackson-databind
- common-events dependency

2. Outbox infrastructure sinifləri (common-kafka):

BaseOutboxEvent sinfi yarat: @MappedSuperclass olsun. Hər service öz modulunda @Entity OutboxEvent extends BaseOutboxEvent yaradacaq və @Table(name="outbox_events") istifadə edəcək. Bu yanaşma hər service-in öz cədvəlini saxlayır, amma field-lər common-kafka-dan paylaşılır:
- id UUID
- aggregateType String
- aggregateId UUID
- eventType String
- payload String (JSON)
- status String: PENDING, PUBLISHED, FAILED
- retryCount int (default 0)
- maxRetries int (default 5)
- nextRetryAt Instant
- createdAt Instant
- publishedAt Instant (nullable)
- errorMessage String (nullable)

BaseOutboxEventRepository<T extends BaseOutboxEvent> generic repository pattern yarat və hər service-də öz OutboxEventRepository-si həmin base repository-ni extend etsin.

OutboxEventService:
- saveEvent(aggregateType, aggregateId, eventType, payload) → OutboxEvent
- saveEvent(aggregateType, aggregateId, eventType, Object payload) → OutboxEvent (auto-serialize)

OutboxPublisherScheduler:
- @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
- PENDING event-ləri oxu (nextRetryAt <= now, retryCount < maxRetries)
- EventEnvelope-ə sarma
- KafkaTemplate ilə publish et
- Uğurlu → status=PUBLISHED, publishedAt=now
- Xəta → retryCount++, nextRetryAt=now+30s*retryCount, errorMessage=exception.message
- retryCount >= maxRetries → status=FAILED

EventEnvelopeFactory:
- createEnvelope(outboxEvent, serviceName) → EventEnvelope<JsonNode>

KafkaPublisher:
- publish(topic, key, EventEnvelope) → void

3. Consumer infrastructure sinifləri (common-kafka):

ProcessedEvent entity:
- id UUID
- eventId UUID (unique)
- eventType String
- consumerName String
- processedAt Instant

ProcessedEventRepository interface.

ProcessedEventService:
- isAlreadyProcessed(eventId, consumerName) → boolean
- markProcessed(eventId, eventType, consumerName) → void

IdempotentEventConsumer abstract class:
- abstract void handleEvent(EventEnvelope<?> envelope)
- final void consume(EventEnvelope<?> envelope):
  1. isAlreadyProcessed yoxla → skip
  2. handleEvent çağır
  3. markProcessed

4. Liquibase reusable changeset template:
common-kafka/src/main/resources/db/outbox-tables-template.xml

Bu template-i hər service öz changelog-unda include edəcək:
- outbox_events cədvəli
- processed_events cədvəli
- Index: status, nextRetryAt, eventId unique

5. Config properties:
- outbox.scheduler.enabled=true
- outbox.scheduler.interval=5000
- outbox.maxRetries=5
- kafka.bootstrap-servers

6. Testlər:
- OutboxEventService save
- OutboxPublisherScheduler success → PUBLISHED
- OutboxPublisherScheduler failure → retry
- OutboxPublisherScheduler max retry → FAILED
- ProcessedEventService duplicate skip
- EventEnvelopeFactory creates valid envelope
- IdempotentEventConsumer skips duplicate

Sonda: yaradılan faylları göstər; hər service-in bu modulu necə istifadə edəcəyini izah et; test command ver.
```

---

## Prompt 5 — InsuranceCompany Model: Entity, Liquibase, Repository, DTO (ƏVVƏLKİ 4-ün 1-ci hissəsi)

```text
User Profile Service-də InsuranceCompany modelini entity/Liquibase/repository/DTO/mapper səviyyəsində əlavə et.

Bu prompt YALNIZ data layer-dir. Service/controller/test növbəti promptdadır.

Hazır vəziyyət:
- IAM/Auth Service role assignment dəstəkləyir.
- User Profile Service-də PatientProfile, DoctorProfile, AgentProfile, Hospital, HospitalBranch, HospitalStaffProfile və DoctorHospitalAssignment var.

1. IAM Service-də yeni rollar əlavə et (Liquibase changeset):
- INSURANCE_ADMIN
- INSURANCE_STAFF

Mövcud rollar qalmalıdır: ADMIN, PATIENT, DOCTOR, AGENT, HOSPITAL_ADMIN, HOSPITAL_STAFF, SYSTEM.

common-security/RoleConstants-a əlavə et:
- INSURANCE_ADMIN = "INSURANCE_ADMIN"
- INSURANCE_STAFF = "INSURANCE_STAFF"

2. User Profile Service-də entity-lər:

InsuranceCompany:
- id UUID
- name String (not null)
- taxId String (unique, not null)
- licenseNumber String (unique, not null)
- email String
- phone String
- address Address (embedded, optional)
- status: PENDING, ACTIVE, SUSPENDED, TERMINATED (enum)
- createdAt Instant
- updatedAt Instant

InsuranceCompanyStaffProfile:
- id UUID
- iamUserId UUID (not null)
- insuranceCompanyId UUID (not null, FK)
- roleType: INSURANCE_ADMIN, INSURANCE_STAFF, AGENT (enum)
- position String (optional)
- employeeCode String (optional)
- status: ACTIVE, SUSPENDED, TERMINATED (enum)
- createdAt Instant
- updatedAt Instant

3. AgentProfile modelini yenilə:
- insuranceCompanyId UUID əlavə et (not null).
- Mövcud AgentProfile entity-sinə field əlavə et.

4. Repository-lər:
- InsuranceCompanyRepository: findByTaxId, findByLicenseNumber, findByStatus
- InsuranceCompanyStaffProfileRepository: findByIamUserId, findByIamUserIdAndInsuranceCompanyId, findAllByInsuranceCompanyId
- AgentProfileRepository-yə əlavə: findByInsuranceCompanyId, findByIamUserIdAndInsuranceCompanyId

5. DTO-lar:
- CreateInsuranceCompanyRequest(name, taxId, licenseNumber, email, phone, address)
- UpdateInsuranceCompanyRequest(name, email, phone, address)
- InsuranceCompanyResponse(id, name, taxId, licenseNumber, email, phone, status, createdAt)
- CreateInsuranceCompanyStaffRequest(iamUserId, roleType, position, employeeCode)
- InsuranceCompanyStaffResponse(id, iamUserId, insuranceCompanyId, roleType, position, employeeCode, status, createdAt)
- LinkAgentToCompanyRequest(insuranceCompanyId)
- AgentCompanyResponse(agentProfileId, insuranceCompanyId, companyName)

6. MapStruct Mapper-lər: InsuranceCompanyMapper, InsuranceCompanyStaffMapper. AgentMapper-i update et.

7. Liquibase changeset-lər (user-profile-service):
- insurance_companies cədvəli
- insurance_company_staff_profiles cədvəli
- agent_profiles-a insurance_company_id column (ALTER TABLE)
- Unique index: tax_id, license_number
- Unique index: iam_user_id + insurance_company_id (staff)
- Unique index: insurance_company_id + employee_code (staff)
- Index: agent_profiles.insurance_company_id
- Flyway istifadə etmə.

Sonda: dəyişən faylları göstər; Liquibase changeset-ləri göstər; gradlew build keçdiyini təsdiqlə.
```

---

## Prompt 6 — InsuranceCompany: Service, Controller, Tests (ƏVVƏLKİ 4-ün 2-ci hissəsi)

```text
InsuranceCompany entity/repository/DTO/mapper artıq hazırdır. İndi service layer, controller və testləri yarat.

1. InsuranceCompanyService:
- createCompany(AuthContext, CreateInsuranceCompanyRequest) → InsuranceCompanyResponse
- getCompany(companyId, AuthContext) → InsuranceCompanyResponse
- getAllCompanies(status, pageable, AuthContext) → Page<InsuranceCompanyResponse>
- updateCompany(companyId, AuthContext, UpdateInsuranceCompanyRequest) → InsuranceCompanyResponse
- changeStatus(companyId, AuthContext, newStatus) → InsuranceCompanyResponse

2. InsuranceCompanyStaffService:
- createStaff(companyId, AuthContext, CreateInsuranceCompanyStaffRequest) → InsuranceCompanyStaffResponse
- getStaffByCompany(companyId, AuthContext, pageable) → Page<InsuranceCompanyStaffResponse>
- getStaffById(companyId, staffId, AuthContext) → InsuranceCompanyStaffResponse
- changeStaffStatus(companyId, staffId, AuthContext, newStatus) → InsuranceCompanyStaffResponse

3. Agent-company link:
- linkAgentToCompany(agentProfileId, companyId, AuthContext) → AgentCompanyResponse
- getAgentsByCompany(companyId, AuthContext, pageable) → Page<AgentProfileResponse>

4. Business rules:
- taxId unique olmalıdır.
- licenseNumber unique olmalıdır.
- Yeni company default PENDING status ilə yaransın.
- ACTIVE olmayan company üçün product/policy yaradılmamalıdır.
- iamUserId + insuranceCompanyId unique olmalıdır.
- employeeCode insuranceCompanyId daxilində unique olmalıdır.
- Agent mütləq insuranceCompanyId-yə bağlı olmalıdır.

5. Security:
- ADMIN bütün company-ləri yarada və idarə edə bilər.
- INSURANCE_ADMIN yalnız öz company datasını görə və staff idarə edə bilər.
- INSURANCE_STAFF yalnız öz company datasını görə bilər.
- AGENT yalnız öz company datasını və öz agent profilini görə bilər.
- PATIENT/DOCTOR/HOSPITAL_ADMIN/HOSPITAL_STAFF company management endpoint-lərinə 403.

6. Controller endpoint-ləri:
- POST /api/v1/insurance-companies
- GET /api/v1/insurance-companies
- GET /api/v1/insurance-companies/{id}
- PUT /api/v1/insurance-companies/{id}
- PATCH /api/v1/insurance-companies/{id}/status
- POST /api/v1/insurance-companies/{companyId}/staff
- GET /api/v1/insurance-companies/{companyId}/staff
- GET /api/v1/insurance-companies/{companyId}/staff/{staffId}
- PATCH /api/v1/insurance-companies/{companyId}/staff/{staffId}/status
- PATCH /api/v1/profiles/agents/{agentProfileId}/insurance-company/{companyId}
- GET /api/v1/profiles/agents/by-company/{companyId}

7. Testlər:
- ADMIN creates insurance company → 201
- default status PENDING
- duplicate taxId → 409
- duplicate licenseNumber → 409
- ADMIN activates company
- INSURANCE_ADMIN sees own company
- INSURANCE_ADMIN cannot access another company → 403
- INSURANCE_ADMIN adds staff to own company
- AGENT linked to company
- PATIENT forbidden → 403
- HOSPITAL_ADMIN forbidden → 403

8. Docs: docs/profile-and-organization-model.md əlavə et.

Sonda: dəyişən faylları göstər; endpoint nümunələri ver; test command-larını yaz.
```

---

## Prompt 7 — InsuranceCompany Ownership Authorization + Internal API (ƏVVƏLKİ 5)

```text
Insurance company ownership authorization modelini tamamla.

Hazır vəziyyət:
- InsuranceCompany entity, service, controller hazırdır.
- InsuranceCompanyStaffProfile və AgentProfile.insuranceCompanyId var.
- common-security AuthContext və RoleChecker var.

Əsas prinsip:
AuthContext.userId → Profile → Organization Scope → Business Resource

1. InsuranceCompanyAccessService yarat:
Metodlar:
- canViewCompany(userId, companyId) → boolean
- canManageCompany(userId, companyId) → boolean
- canManageCompanyStaff(userId, companyId) → boolean
- canAgentOperateForCompany(userId, companyId) → boolean
- getCurrentUserInsuranceCompanyId(AuthContext) → UUID (nullable)
- requireCanViewCompany(AuthContext, companyId) → void (throws 403)
- requireCanManageCompany(AuthContext, companyId) → void
- requireAgentOrInsuranceStaffInCompany(AuthContext, companyId) → void
- resolveCompanyScopeForCurrentUser(AuthContext) → UUID (nullable)

2. Lookup:
- INSURANCE_ADMIN/INSURANCE_STAFF: AuthContext.userId → InsuranceCompanyStaffProfile → insuranceCompanyId
- AGENT: AuthContext.userId → AgentProfile → insuranceCompanyId
- ADMIN: bypass (qlobal access)
- Digər rollar: null (company scope yoxdur)

3. Mövcud endpoint-lərə tətbiq et (Prompt 6-da yaradılan controller-lər).

4. Internal API əlavə et:
Base: /internal/v1/insurance-companies

- GET /internal/v1/insurance-companies/{companyId}/exists-active → boolean
- GET /internal/v1/users/{iamUserId}/insurance-scope → InsuranceScopeResponse(iamUserId, insuranceCompanyId, roles, canView, canManage, isAgent, isInsuranceAdmin, isInsuranceStaff)
- GET /internal/v1/agents/{agentProfileId}/insurance-company → AgentCompanyResponse
- GET /internal/v1/insurance-companies/{companyId}/access/current → AccessCheckResponse

5. Internal security: X-Internal-Service-Secret tələb et. Secret config-dən oxunsun. hardcode etmə. invalid/missing secret üçün 401.

6. Liquibase: əgər index yoxdursa əlavə et — staff iam_user_id, insurance_company_id, agent iam_user_id, insurance_company_id.

7. Testlər:
- ADMIN can access all companies
- INSURANCE_ADMIN can manage own company
- INSURANCE_ADMIN cannot manage another company → 403
- INSURANCE_STAFF can view own company
- INSURANCE_STAFF cannot manage staff → 403
- AGENT can operate only own company
- PATIENT forbidden → 403
- internal endpoint valid secret → 200
- internal endpoint invalid secret → 401

8. Docs: docs/security-ownership-model.md əlavə et.

Sonda: access matrix ver; dəyişən faylları göstər; test command-larını yaz.
```

---

## Prompt 8 — User Profile Final Hardening (ƏVVƏLKİ 6)

```text
User Profile Service-i final multi-company və provider-aware vəziyyətə gətir.

Hazır vəziyyət: patient, doctor, agent, hospital, insurance company, staff, ownership authorization — hamısı hazırdır.

Tapşırıqlar:

1. Patient Profile API-lərini yoxla/tamamla: POST/GET/PUT/search
2. Doctor Profile API-lərini yoxla/tamamla: POST/GET/PUT/search
3. Agent Profile API-lərini yoxla/tamamla: POST/GET/PUT/search + by-company
4. Hospital API-lərini yoxla/tamamla: POST/GET/PUT/status + branch + staff + doctor assignment

5. Search endpoint-lərdə pagination və filter: status, name, email, companyId, hospitalId, city, specialty.

6. Ownership rules: patient öz profilini, doctor öz profilini, agent öz profilini + company scope, hospital admin öz hospitalı, insurance admin öz company agent/staff, admin global.

7. Internal summary API-lər:
- GET /internal/v1/profiles/users/{iamUserId}/summary
- GET /internal/v1/profiles/patients/{patientProfileId}/exists
- GET /internal/v1/profiles/doctors/{doctorProfileId}/exists
- GET /internal/v1/profiles/hospitals/{hospitalId}/exists-active
- GET /internal/v1/profiles/insurance-companies/{companyId}/exists-active

8. Validation: email format, phone format, future dateOfBirth olmasın, licenseNumber unique, employeeCode scope unique, taxId/license unique.

9. Liquibase: çatışmayan index və constraint-ləri yeni changeset ilə əlavə et.

10. Testlər: patient ownership, doctor ownership, agent company scope, hospital admin own hospital, insurance admin own company, admin global, internal endpoints valid/invalid secret.

Sonda: User Profile Service access matrix; dəyişən faylları göstər; test command-larını yaz.
```

---

## Prompt 9 — Policy Service Schema (ƏVVƏLKİ 7 + Migration Fix)

```text
Policy Service-i çox şirkətli modelə uyğun schema/repository/DTO/mapper səviyyəsində tamamla.

⚠️ VACIB MIGRATION: Mövcud kodda InsurancePlan entity var. Bu InsuranceProduct-a dəyişir.
Mövcud InsurancePlan.java faylını SİL və InsuranceProduct.java yarat.
Mövcud CoverageRule entity-sində planId field-ı productId-yə dəyişir.
Mövcud Policy entity-sində planId field-ı productId-yə dəyişir.
Liquibase changeset ilə mövcud insurance_plan cədvəlini insurance_products-a rename et (əgər cədvəl varsa).
Əgər cədvəl hələ yaradılmayıbsa (boş skeleton), sadəcə yeni cədvəl yarat.

Entity-lər:

1. InsuranceProduct:
- id, insuranceCompanyId, productCode, name, description
- coverageType: BASIC, STANDARD, PREMIUM, CUSTOM (enum)
- premiumAmount (BigDecimal), annualLimit (BigDecimal), currency (default AZN)
- status: DRAFT, ACTIVE, INACTIVE, ARCHIVED (enum)
- createdAt, updatedAt

2. CoverageRule:
- id, productId (əvvəl planId idi), serviceType: HOSPITAL/DENTAL/OPTICAL/MENTAL_HEALTH/PHARMACY/LAB
- coveragePercent (int 0-100), maxAmount (BigDecimal), waitingPeriodDays (int)
- requiresPreApproval (boolean), status: ACTIVE/INACTIVE
- createdAt, updatedAt

3. Policy:
- id, policyNumber (unique), insuranceCompanyId, productId (əvvəl planId idi)
- patientProfileId, agentProfileId (optional)
- status: DRAFT, PAYMENT_PENDING, ACTIVE, EXPIRED, CANCELLED, SUSPENDED
- startDate, endDate, premiumAmount, annualLimit, usedLimit, reservedLimit
- createdAt, updatedAt, @Version (optimistic locking)

4. ProviderContract (YENİ):
- id, insuranceCompanyId, hospitalId, productId (optional)
- contractNumber (unique per company), startDate, endDate
- status: ACTIVE, INACTIVE, EXPIRED, TERMINATED
- payoutModel: DIRECT_TO_HOSPITAL, REIMBURSE_PATIENT
- createdAt, updatedAt

5. PolicyLimitReservation:
- id, policyId, insuranceCompanyId, claimId, reservedAmount
- status: RESERVED, RELEASED, COMMITTED
- createdAt, updatedAt

Repository, DTO, MapStruct Mapper-lar yarat.

Liquibase changeset-lər:
- insurance_products (rename və ya yeni yarat)
- coverage_rules (planId → productId rename əgər lazımdırsa)
- policies (planId → productId, policyNumber, yeni field-lər)
- provider_contracts (yeni cədvəl)
- policy_limit_reservations (yeni field-lər)
- İndex-lər: company_id, product_code+company_id unique, policy_number unique, contract_number+company_id unique

build.gradle-a spring-data-redis, spring-cache əlavə et.

Testlər: repository save/find, mapper tests, schema validation, duplicate productCode same company fail, same productCode different companies OK.

Sonda: dəyişən faylları göstər; migration strategiyasını izah et; test command-larını yaz.
```

---

## Prompt 10 — Policy Service Business API Part 1: Product, Coverage, Contract (ƏVVƏLKİ 8-in 1-ci hissəsi)

```text
Policy Service-in Product, CoverageRule və ProviderContract business API-lərini implementasiya et.

Schema/repository/DTO/mapper artıq hazırdır.

1. InsuranceProductService:
- createProduct(AuthContext, CreateInsuranceProductRequest) → InsuranceProductResponse
- updateProduct(AuthContext, productId, UpdateInsuranceProductRequest)
- changeStatus(AuthContext, productId, status)
- getProduct(productId, AuthContext)
- searchProducts(companyId, status, pageable)

Rules: ADMIN bütün company, INSURANCE_ADMIN öz company, AGENT öz company ACTIVE, PATIENT ACTIVE products, productCode company daxilində unique.

2. CoverageRuleService:
- addCoverageRule(AuthContext, productId, CreateCoverageRuleRequest)
- updateCoverageRule(AuthContext, ruleId, request)
- changeStatus(AuthContext, ruleId, status)
- getRulesByProduct(productId)

Rules: ADMIN/INSURANCE_ADMIN öz company products, coveragePercent 0-100, maxAmount >= 0.

3. ProviderContractService:
- createContract(AuthContext, CreateProviderContractRequest)
- getContract(id, AuthContext)
- getContractsByCompany(companyId, AuthContext)
- getContractsByHospital(hospitalId, AuthContext)
- terminateContract(id, AuthContext)

Rules: ADMIN/INSURANCE_ADMIN öz company, HOSPITAL_ADMIN öz hospital contracts görə bilər, ACTIVE deyilsə in-network sayılmasın.

4. @Cacheable(Redis) ilə ACTIVE products və coverage rules keşlə (TTL: 10 dəq). Redis failure → DB fallback.

5. Controllers:
- InsuranceProductController: /api/v1/insurance-products
- CoverageRuleController: /api/v1/insurance-products/{productId}/coverage-rules
- ProviderContractController: /api/v1/provider-contracts

6. Testlər: insurance admin creates product own company, cannot create another company product, agent cannot create, coverage rule validation, provider contract create/terminate, hospital admin sees own contracts, Redis cache hit/miss.

Sonda: endpoint nümunələri ver; test command-larını yaz.
```

---

## Prompt 11 — Policy Service Business API Part 2: Policy, Eligibility, Limits, Internal API (ƏVVƏLKİ 8-in 2-ci hissəsi)

```text
Policy Service-in Policy lifecycle, eligibility check, limit reservation və internal API-lərini implementasiya et.

Product/CoverageRule/ProviderContract artıq hazırdır.

1. PolicyService:
- issuePolicy(AuthContext, IssuePolicyRequest)
- getPolicy(policyId, AuthContext)
- getMyPolicies(AuthContext)
- searchPolicies(companyId, patientProfileId, status, pageable)
- activatePolicy(policyId) — internal, payment tamamlandıqdan sonra
- cancelPolicy(policyId, AuthContext, reason)
- suspendPolicy(policyId, AuthContext, reason)

Rules: Product ACTIVE olmalı, insuranceCompanyId product-dan, PatientProfile mövcud, Agent eyni company, PAYMENT_PENDING → payment complete → ACTIVE, Patient öz policy-ləri, Agent öz company, ADMIN global.

2. EligibilityService:
POST /api/v1/policies/eligibility-check
Request: policyId, insuranceCompanyId, patientProfileId, hospitalId(opt), serviceType, claimAmount, treatmentDate

Rules: Policy ACTIVE, insuranceCompanyId uyğun, patientProfileId uyğun, treatmentDate range içində, Product ACTIVE, CoverageRule ACTIVE, waiting period keçmiş, availableLimit = annualLimit - usedLimit - reservedLimit >= claimAmount, hospitalId varsa ProviderContract yoxla, coveredAmount = min(claimAmount * coveragePercent/100, maxAmount).

3. PolicyLimitService:
- reserveLimit(policyId, claimId, companyId, amount) — optimistic lock retry
- commitReservation(reservationId)
- releaseReservation(reservationId, reason)

Rules: duplicate RESERVED claimId olmasın, commit/release transitions düzgün.

4. Outbox integration: PolicyCreatedEvent, PolicyActivatedEvent, PolicyCancelledEvent — outbox-a yaz (common-kafka istifadə et).

5. Internal API: /internal/v1/policies
- GET /{policyId}
- GET /{policyId}/active
- POST /eligibility-check
- POST /{policyId}/limit-reservations
- PUT /limit-reservations/{reservationId}/confirm
- PUT /limit-reservations/{reservationId}/release
- PUT /{policyId}/activate-after-payment
Internal security: X-Internal-Service-Secret.

6. PolicyController: /api/v1/policies

7. Liquibase: outbox_events, processed_events cədvəlləri (common-kafka template-dən).

8. Testlər: issue policy, payment pending, patient sees own, agent own company, eligibility in-network, eligibility out-of-network, waiting period fail, limit exceeded, reserve/commit/release, concurrent reservation, internal secret test.

Sonda: endpoint nümunələri, eligibility calculation docs, test command-lar.
```

---

## Prompt 12 — Payment Service (ƏVVƏLKİ 9)

```text
Payment Service-i multi-company policy payment və payout flow üçün implementasiya et.

Entity-lər:

Payment: id, paymentNumber(unique), policyId(opt), insuranceCompanyId, patientProfileId(opt), claimId(opt), hospitalId(opt), amount, currency(AZN), paymentType(POLICY_PREMIUM/CLAIM_PAYOUT/HOSPITAL_PAYOUT/REFUND), status(PENDING/COMPLETED/FAILED/CANCELLED/REFUNDED), provider(MOCK), providerReference(opt), createdAt, updatedAt.

PaymentTransaction: id, paymentId, transactionType(AUTHORIZE/CAPTURE/REFUND/PAYOUT), amount, status(SUCCESS/FAILED), providerResponse(opt), createdAt.

Invoice: id, invoiceNumber(unique), policyId(opt), claimId(opt), insuranceCompanyId, hospitalId(opt), patientProfileId(opt), amount, currency, status(DRAFT/ISSUED/PAID/CANCELLED), issuedAt, paidAt(opt), createdAt, updatedAt.

Repository, DTO, Mapper yarat.

MockPaymentGatewayClient: default olaraq uğurlu nəticə qaytarsın. Failure simulyasiyası config ilə idarə olunsun: PAYMENT_MOCK_FAILURE_RATE=${PAYMENT_MOCK_FAILURE_RATE:0.0}. Local demo üçün 0.05 verilə bilər, testlərdə 0.0 olmalıdır ki, testlər flaky olmasın.

Endpoint-lər: POST /payments/policy-premium, POST /{id}/complete-mock, POST /{id}/fail-mock, POST /{id}/refund-mock, POST /payments/claim-payout, GET /{id}, GET /my, GET /by-policy, /by-claim, /by-company, /by-hospital. Invoice CRUD endpoints.

Business rules: Payment COMPLETED olduqda Policy activate-after-payment çağır (PolicyInternalClient). Mock complete → COMPLETED, fail → FAILED. COMPLETED payment refund edilə bilər. Hər status dəyişikliyində PaymentTransaction.

Outbox: PaymentCompletedEvent, PaymentFailedEvent, ClaimPayoutCompletedEvent, ClaimPayoutFailedEvent.

Security: PATIENT öz, AGENT öz company, INSURANCE_ADMIN öz company, HOSPITAL_ADMIN öz hospital payouts, ADMIN hər şey.

Liquibase: payments, payment_transactions, invoices, outbox_events, processed_events. Index-lər.

Testlər: create premium payment, complete activates policy, failed doesn't activate, refund, agent scope, patient own, hospital payout, invoice lifecycle.

Sonda: payment-policy activation flow docs; test command-lar.
```

---

## Prompt 13 — Claim Schema/Repository/DTO/Mapper (ƏVVƏLKİ 10)

```text
Claim Service-i multi-company modelə uyğun schema/repository/DTO/mapper tamamla.

Entity-lər:

Claim: id, claimNumber(unique), policyId, insuranceCompanyId, patientProfileId, hospitalId(opt), doctorProfileId(opt), status(DRAFT/SUBMITTED/UNDER_REVIEW/NEEDS_MORE_DOCUMENTS/APPROVED/REJECTED/PAYMENT_PENDING/PAID/PAYOUT_FAILED/CANCELLED), serviceType, treatmentDate, claimAmount, approvedAmount(opt), coveredAmount, patientPayAmount, payoutRecipientType(PATIENT/HOSPITAL), policyReservationId(opt), diagnosisCode(opt), reason(opt), notes(opt), riskScore(opt), riskLevel(opt), fraudScore(opt), fraudLevel(opt), fraudPassed(opt), createdAt, updatedAt.

ClaimItem: id, claimId, description, serviceCode(opt), amount, quantity, serviceDate(opt), documentId(opt), createdAt, updatedAt.

ClaimDocumentReference: id, claimId, documentId, documentType, required(boolean), status(ATTACHED/MISSING/REJECTED), createdAt.

ClaimDecision: id, claimId, decision(APPROVED/REJECTED), decidedBy UUID, reason, approvedAmount(opt), createdAt.

ClaimStatusHistory: id, claimId, fromStatus, toStatus, changedByUserId(opt), reason(opt), createdAt.

Repository, DTO, MapStruct Mapper yarat.

Liquibase: claims, claim_items, claim_document_references, claim_decisions, claim_status_history, outbox_events, processed_events. Index: claim_number unique, policy_id, insurance_company_id, patient_profile_id, hospital_id, status, treatment_date.

Testlər: schema validation, mapper tests, repository save/find, find by company/patient/hospital.

Sonda: dəyişən faylları göstər; Liquibase changeset-ləri göstər; test command-lar.
```

---

## Prompt 14 — Claim Business Lifecycle (ƏVVƏLKİ 11-in 1-ci hissəsi)

```text
Claim Service business lifecycle və state machine implementasiya et. Kafka/Feign config növbəti promptda.

Schema/repository/DTO/mapper hazırdır.

1. ClaimService:
- createClaim(AuthContext, CreateClaimRequest) → ClaimResponse
- addClaimItem(AuthContext, claimId, ClaimItemRequest) → ClaimItemResponse
- attachDocument(AuthContext, claimId, AttachClaimDocumentRequest) → void
- submitClaim(AuthContext, claimId) → ClaimResponse
- getClaimById(AuthContext, claimId) → ClaimResponse
- getMyClaims(AuthContext, pageable) → Page<ClaimSummaryResponse>
- searchClaims(AuthContext, filters, pageable) → Page<ClaimSummaryResponse>

Submit flow:
1. Claim DRAFT olmalıdır.
2. Ən azı 1 claim item olmalıdır.
3. claimAmount = sum(items.amount * items.quantity)
4. PolicyInternalClient-dən policy detail al.
5. insuranceCompanyId policy-dən götür.
6. patientProfileId policy ilə uyğun olmalıdır.
7. Eligibility check çağır.
8. eligible=false → submit bloklanır (exception).
9. coveredAmount, patientPayAmount, payoutRecipientType set et.
10. Policy reserve-limit çağır.
11. policyReservationId saxla.
12. Status → SUBMITTED.
13. ClaimStatusHistory yaz.
14. OutboxEvent ClaimSubmittedEvent yaz (common-kafka OutboxEventService istifadə et).

2. ClaimReviewService:
- startReview(AuthContext, claimId) — SUBMITTED → UNDER_REVIEW
- requestMoreDocuments(AuthContext, claimId, reason) — UNDER_REVIEW → NEEDS_MORE_DOCUMENTS
- approveClaim(AuthContext, claimId, ReviewClaimRequest) — UNDER_REVIEW → APPROVED
- rejectClaim(AuthContext, claimId, ReviewClaimRequest) — UNDER_REVIEW → REJECTED
- cancelClaim(AuthContext, claimId, reason) — DRAFT/SUBMITTED → CANCELLED

Approve: approvedAmount <= coveredAmount, reservation commit, ClaimDecision yaz, ClaimApprovedEvent outbox-a. Payout request-i Payment Service ClaimApprovedEvent consumer-i yaradacaq; Claim Service ayrıca ClaimPayoutRequestedEvent yazmasın ki, duplicate payment yaranmasın.
Reject: reason məcburi, reservation release, ClaimDecision yaz, ClaimRejectedEvent outbox-a.

3. Security: PATIENT öz policy claim, HOSPITAL_STAFF öz hospital claim, AGENT öz company review, INSURANCE_ADMIN öz company, ADMIN global.

4. Internal API: /internal/v1/claims — GET /{claimId}, GET /{claimId}/summary, GET /by-patient/summary, GET /by-company/summary, GET /by-hospital/summary. X-Internal-Service-Secret.

5. Controllers: ClaimController, ClaimReviewController, ClaimInternalController.

6. ClaimException sinfi yarat.

Testlər: patient claim create, hospital staff claim, submit success, submit without item fail, submit not eligible fail, agent own company review, agent other company 403, approve, reject, invalid transitions, outbox event created.

Sonda: claim lifecycle docs; test command-lar.
```

---

## Prompt 15 — Claim Kafka Consumers, Feign Config, OutboxWorker (ƏVVƏLKİ 11-in 2-ci hissəsi + 12)

```text
Claim Service-ə Kafka consumer-lar, Feign client-lər və OutboxWorker əlavə et.

Hazır: Claim business lifecycle, outbox event-lər yazılır.

1. Kafka Consumers (common-kafka IdempotentEventConsumer-dən extend et):
- RiskAnalysisConsumer: RiskAnalysisCompletedEvent → Claim.riskScore, riskLevel update
- FraudCheckConsumer: FraudCheckCompletedEvent → Claim.fraudScore, fraudLevel, fraudPassed update
- PaymentConsumer: ClaimPayoutCompletedEvent → APPROVED → PAID, ClaimPayoutFailedEvent → payout failed marker
Hər consumer ConsumerProcessedEvent ilə idempotent olsun.

2. OutboxWorker: common-kafka OutboxPublisherScheduler-dən istifadə et. @Scheduled hər 5 saniyə. Outbox-dan PENDING event-ləri oxu, Kafka-ya publish et.

3. Feign/WebClient client-lər:
- PolicyInternalClient (policy-service Eureka üzərindən)
- UserProfileInternalClient (user-profile-service)
- HealthRecordInternalClient (optional, document validation üçün)
- connect timeout: 2s, read timeout: 5s
- X-Internal-Service-Secret header avtomatik əlavə
- X-Correlation-Id propagation

4. KafkaConsumerConfig: groupId: claim-service-group, topics: risk.events, fraud.events, payment.events, JsonDeserializer, error handler, retry/backoff.
KafkaProducerConfig: key String, value JSON, EventEnvelope publish.

5. application.yml/config update: kafka bootstrap, consumer group, feign timeout, internal secret, outbox scheduler interval.

6. build.gradle: spring-kafka, spring-cloud-starter-openfeign, common-kafka dependency.

7. Testlər: producer serializes EventEnvelope, consumer duplicate event skip, Feign adds internal secret, timeout config applied, risk consumer idempotency, fraud consumer idempotency, payment consumer idempotency.

Sonda: dəyişən faylları göstər; config nümunələri; test command-lar.
```

---

## Prompt 16 — Health Record Schema (ƏVVƏLKİ 13)

```text
Health Record Service-i schema/repository/DTO/mapper səviyyəsində tamamla.

Entity-lər:

HealthRecord: id, patientProfileId, doctorProfileId(opt), hospitalId(opt), branchId(opt), claimId(opt), visitDate, visitType(INPATIENT/OUTPATIENT/EMERGENCY), recordType(CONSULTATION/LAB_RESULT/DIAGNOSIS/PRESCRIPTION/PROCEDURE), diagnosis, notes, status(ACTIVE/ARCHIVED), createdAt, updatedAt.

Treatment: id, healthRecordId, serviceType, treatmentType, description, startDate, endDate(opt), medications(opt), estimatedCost(opt), actualCost(opt), createdAt, updatedAt.

MedicalDocument: id, healthRecordId(opt), treatmentId(opt), claimId(opt), patientProfileId, hospitalId(opt), uploadedByUserId, documentType(LAB_RESULT/PRESCRIPTION/IMAGING/DISCHARGE_SUMMARY/INVOICE/DOCTOR_REPORT/OTHER), fileName, fileSize, contentType, storageBucket, minioKey, sha256Hash, status(PENDING_UPLOAD/CONFIRMED/UPLOAD_FAILED), createdAt, updatedAt.

DocumentHashIndex: id, sha256Hash, documentId, patientProfileId, claimId(opt), hospitalId(opt), createdAt.

HealthAccessLog: id, healthRecordId(opt), documentId(opt), accessedByUserId, accessRole, reason, accessedAt.

Repository, DTO, Mapper yarat.

Liquibase: health_records, treatments, medical_documents, document_hash_index, health_access_log, outbox_events, processed_events. Index-lər.

build.gradle: io.minio:minio:8.5.7, spring-kafka, common-kafka dependency.

Testlər: repository save/find, mapper, schema validation, find docs by claim/patient/hospital, duplicate hash index.

Sonda: dəyişən faylları; Liquibase changeset-ləri; test command-lar.
```

---

## Prompt 17 — Health Record MinIO Service + API (ƏVVƏLKİ 14)

```text
Health Record Service-in MinIO upload/download, service layer və internal API-lərini tamamla.

1. MinioStorageService: ensureBucketExists, generatePresignedUploadUrl(15 dəq), generatePresignedDownloadUrl(5 dəq), deleteObject. Bucket: medical-documents.

2. HealthRecordService: createHealthRecord, getMyHealthRecords, getHealthRecord(+access log), getHealthRecordsByClaim, addTreatment, archiveHealthRecord.

3. MedicalDocumentService:
- initiateUpload → PENDING_UPLOAD yarat, MinIO presigned PUT URL qaytar
- confirmUpload → sha256Hash qəbul et, CONFIRMED, DocumentHashIndex yaz, MedicalDocumentConfirmedEvent outbox-a
- getDocument → metadata + presigned download URL + access log
- deleteDocument → MinIO sil + soft delete

4. Internal API: /internal/v1/health-records
- GET /documents/{documentId}/hash → sha256Hash, patientProfileId, claimId, hospitalId
- GET /documents/by-claim/{claimId}
- GET /documents/{documentId}/summary
Internal security: X-Internal-Service-Secret.

5. Security: PATIENT öz records, DOCTOR assignment scope, HOSPITAL_STAFF öz hospital upload, HOSPITAL_ADMIN öz hospital docs, AGENT/INSURANCE_ADMIN claim review üçün claimId docs, ADMIN global.

6. Outbox: common-kafka OutboxEventService istifadə et.

7. Controllers: HealthRecordController /api/v1/health-records, MedicalDocumentController /api/v1/health-records/{id}/documents.

8. Testlər: initiate upload, confirm upload, sha256 saved, DocumentHashIndex written, download URL, access log, patient own, hospital admin own, agent claim docs, MinIO failure, internal hash API valid/invalid secret.

Sonda: MinIO usage docs; upload/download nümunəsi; test command-lar.
```

---

## Prompt 18 — Fraud Detection Service (ƏVVƏLKİ 15)

```text
Fraud Detection Service-i multi-company rule-based fraud scoring kimi implementasiya et.

Entity-lər:

FraudAssessment: id, claimId, insuranceCompanyId, patientProfileId, hospitalId(opt), doctorProfileId(opt), fraudScore(BigDecimal 0-1), fraudLevel(LOW/MEDIUM/HIGH/CRITICAL), manualReviewRequired(boolean), status(PENDING/COMPLETED/FAILED), createdAt, completedAt.

FraudSignal: id, fraudAssessmentId, signalType(DUPLICATE_DOCUMENT/FREQUENT_CLAIMS/HIGH_AMOUNT/SUSPICIOUS_TIMING/HOSPITAL_ANOMALY/DOCTOR_ANOMALY/WAITING_PERIOD_VIOLATION), severity(LOW/MEDIUM/HIGH), scoreImpact(BigDecimal), message, createdAt.

DocumentHashIndex: id, sha256Hash, claimId, patientProfileId, insuranceCompanyId, hospitalId(opt), createdAt.

Architecture: FraudRule interface, FraudContext, FraudRuleResult, FraudScoringService, FraudAssessmentService.

Rules: DuplicateDocumentRule, FrequentClaimsRule(30 gündə 3+), HighAmountRule(3x ortalama), SuspiciousTimingRule(policy 7 gün), WaitingPeriodRule, HospitalAnomalyRule, DoctorAnomalyRule.

Kafka: consume ClaimSubmittedEvent (common-kafka IdempotentEventConsumer), FraudAssessment PENDING yarat, rules run, FraudCheckCompletedEvent outbox-a publish.

Integration: HealthRecordInternalClient (doc hash), PolicyInternalClient(opt), ClaimInternalClient.

Endpoint-lər: POST /fraud/claims/{claimId}/check, GET /fraud/claims/{claimId}, GET /fraud/assessments/{id}, GET /fraud/companies/{companyId}/summary, GET /fraud/hospitals/{hospitalId}/summary.

Security: ADMIN all, INSURANCE_ADMIN own company, AGENT own company, HOSPITAL_ADMIN own hospital limited, PATIENT forbidden.

Liquibase: fraud_assessments, fraud_signals, document_hash_index, outbox_events, processed_events.

Testlər: duplicate doc rule, frequent claims, high amount, suspicious timing, full fraud check, Kafka consumer idempotency, patient forbidden, agent scope.

Sonda: fraud scoring docs; test command-lar.
```

---

## Prompt 19 — AI Risk Service (ƏVVƏLKİ 16)

```text
AI Risk Service-i external provider inteqrasiyası kimi implementasiya et.

Config: AI_PROVIDER_BASE_URL, AI_API_KEY, AI_MODEL_NAME, AI_TIMEOUT_SECONDS, AI_ENABLED=true/false.

Entity-lər:
AiRiskAssessment: id, claimId, insuranceCompanyId, patientProfileId, policyId, hospitalId(opt), doctorProfileId(opt), riskScore, riskLevel(LOW/MEDIUM/HIGH/CRITICAL), confidence, reasons(List<String> JSON), rawProviderResponse(opt), status(PENDING/SUCCESS/FAILED/FALLBACK_USED), createdAt, completedAt.

AiRequestLog: id, claimId, requestPayload, responsePayload(opt), providerName, model, status, errorMessage(opt), durationMs, createdAt.

Architecture: RiskModelClient interface, ExternalAiRiskModelClient(RestClient → /chat/completions), FallbackRiskModelClient(AI_ENABLED=false və ya timeout), AiPromptBuilder(PII minimized), AiRiskAssessmentService.

Logic: Claim data + policy context + fraud summary → PII minimized payload → external AI → timeout/retry → fallback if failure → AiRequestLog → AiRiskAssessment → RiskAnalysisCompletedEvent outbox-a.

Kafka: consume ClaimSubmittedEvent, publish RiskAnalysisCompletedEvent. common-kafka IdempotentEventConsumer.

Endpoint-lər: POST /ai-risk/claims/{claimId}/assess, GET /ai-risk/claims/{claimId}, GET /ai-risk/assessments/{id}, GET /ai-risk/companies/{companyId}/summary. rawProviderResponse yalnız ADMIN.

Security: ADMIN all, INSURANCE_ADMIN own company, AGENT own company, PATIENT forbidden.

Liquibase: ai_risk_assessments, ai_request_logs, outbox_events, processed_events.

Testlər: successful AI assessment, AI disabled fallback, provider timeout fallback, PII not included, Kafka idempotency, patient forbidden, agent scope.

Sonda: AI payload nümunəsi; .env.example yenilə; test command-lar.
```

---

## Prompt 20 — Notification Service (ƏVVƏLKİ 17)

```text
Notification Service-i template, mock sender və event consumer modeli ilə tamamla.

Entity-lər:
Notification: id, recipientUserId, recipientEmail(opt), recipientPhone(opt), channel(EMAIL/SMS/IN_APP), templateCode, subject(opt), message, status(PENDING/SENT/FAILED/PERMANENTLY_FAILED), retryCount, maxRetries(default 3), relatedEntityType, relatedEntityId, insuranceCompanyId(opt), hospitalId(opt), createdAt, sentAt(opt), updatedAt, errorMessage(opt).

NotificationTemplate: id, templateCode, channel, subjectTemplate(opt), bodyTemplate, status(ACTIVE/INACTIVE), createdAt.

NotificationRetry: id, notificationId, attemptNumber, failureReason, attemptedAt.

Sender-lər: NotificationSender interface, EmailMockSender(log), SmsMockSender(log), InAppNotificationSender.

TemplateRenderer: Mustache və ya sadə placeholder.

Default templates (Liquibase seed): PASSWORD_RESET, INSURANCE_COMPANY_CREATED, POLICY_CREATED, POLICY_ACTIVATED, PAYMENT_COMPLETED, PAYMENT_FAILED, CLAIM_SUBMITTED, CLAIM_APPROVED, CLAIM_REJECTED, CLAIM_NEEDS_MORE_DOCUMENTS.

Kafka consumers (common-kafka IdempotentEventConsumer):
- InsuranceCompanyCreatedEvent, PolicyCreatedEvent, PolicyActivatedEvent, PaymentCompletedEvent, PaymentFailedEvent, ClaimSubmittedEvent, ClaimApprovedEvent, ClaimRejectedEvent, ClaimNeedsMoreDocumentsEvent, NotificationRequestedEvent.

RetryScheduler: @Scheduled hər 1 dəq, FAILED + retryCount < maxRetries → yenidən, maxRetries keçilərsə PERMANENTLY_FAILED.

Endpoint-lər: POST /notifications/send, GET /{id}, GET /my, GET /by-user/{userId}, GET /by-company/{companyId}. Template CRUD: POST/GET/PUT/PATCH status.

Security: ADMIN all, PATIENT own, INSURANCE_ADMIN own company, template management ADMIN only.

Liquibase: notifications, notification_templates, notification_retries, seed templates, outbox_events, processed_events.

Testlər: template render, mock email/SMS, failed retry, permanently failed, event consumer creates notification, patient own, company scope.

Sonda: template siyahısı; test command-lar.
```

---

## Prompt 21 — Event Integration və Cross-service Flow (ƏVVƏLKİ 19)

```text
Core service-ləri event-driven flow-a qoş.

Hazır: common-kafka outbox/consumer infra, bütün service-lər event publish/consume edir.

Event publishing yoxla/tamamla:
- IAM: role assignment → UserRoleAssignedEvent
- User Profile: company created/activated, hospital created, staff created
- Policy: policy created/activated/cancelled
- Payment: payment completed/failed, payout completed/failed
- Claim: claim submitted/approved/rejected/paid
- Health Record: document confirmed
- AI Risk: risk completed
- Fraud: fraud completed

Consumer-lar yoxla/tamamla:
- AI Risk: ClaimSubmittedEvent → assessment
- Fraud: ClaimSubmittedEvent → check
- Payment: ClaimApprovedEvent → payout request/payment create
- Notification: 10+ event → notification create
- Claim: risk/fraud/payment events → field updates

Rules:
- Claim submit response AI/Fraud gözləməsin (async).
- Duplicate event duplicate payment/notification yaratmasın.
- Consumer idempotency mandatory (ProcessedEventService).
- DLT əlavə et: hər topic-in .dlt versiyası, max retry keçənlər DLT-yə.

Testlər: ClaimSubmittedEvent → AI + Fraud assessment yaranır, ClaimApprovedEvent → payout yaranır, PaymentCompletedEvent → notification yaranır, duplicate event skip, consumer failure retry, claim risk/fraud fields updated, claim paid after payout.

Sonda: event sequence docs; event mapping table; test command-lar.
```

---

## Prompt 22 — Document Hash Cross-check (ƏVVƏLKİ 20)

```text
Health Record, Claim və Fraud arasında document duplicate fraud check inteqrasiyasını tamamla.

1. ClaimItemRequest-ə documentId (nullable UUID) dəstəklənsin.
2. ClaimSubmittedEvent-ə documentIds list əlavə et.

3. Health Record internal API: POST /internal/v1/documents/hash/batch — documentIds → list of {documentId, sha256Hash, patientProfileId, claimId, hospitalId}.

4. Fraud DuplicateDocumentRule: ClaimSubmittedEvent documentIds → health record batch hash API → fraud DB DocumentHashIndex ilə müqayisə → eyni hash başqa claim-də → FraudSignal. Fraud check bitdikdən sonra hash-ləri DocumentHashIndex-ə yaz.

5. Security: internal API X-Internal-Service-Secret.

6. Testlər: batch hash API success, invalid secret fail, duplicate signal, new document no signal, hash saved after check.

Sonda: integration flow docs; test command-lar.
```

---

## Prompt 23 — OpenAPI + Postman Collection (ƏVVƏLKİ 21)

```text
Bütün servis-lər üçün OpenAPI/Swagger sənədləşməsi və Postman collection hazırla.

1. springdoc-openapi-starter-webmvc-ui bütün build.gradle-lara əlavə et.

2. Hər service üçün OpenApiConfig: title, version 1.0, description, BearerAuth security scheme.

3. application.yml: springdoc.swagger-ui.path=/swagger-ui.html, springdoc.api-docs.path=/api-docs.

4. Controller-lərdə @Operation, @ApiResponse annotation-lar.

5. Postman collection: docs/postman_collection.json
Full flow: Admin login → company create → activate → insurance admin login → agent link → product → coverage → hospital → branch → doctor assign → provider contract → patient register/login → profile → policy → premium payment → mock complete → health record → document upload/confirm → claim create → item add → submit → fraud/AI check → agent review → approve → payout → notification.

6. Postman variables: base_url, gateway_url, access_token, patient_token, agent_token, insurance_admin_token, policy_id, claim_id, etc. Token auto-save scripts.

7. README-də Swagger linklərini əlavə et.

Sonda: Swagger link list; Postman path; istifadə qaydası.
```

---

## Prompt 24 — Unit Tests (ƏVVƏLKİ 22-nin 1-ci hissəsi)

```text
SaglamOL üçün kritik unit testləri yaz.

JUnit 5 + Mockito. Real DB lazım deyil — bütün repo-lar mock.

Test sinifləri:

1. common-security: AuthContext resolve, RoleChecker
2. InsuranceCompanyAccessService: admin bypass, insurance admin own company, agent own company, patient forbidden
3. ProviderAccessService: hospital admin own hospital, doctor assignment scope
4. PolicyService: product ACTIVE check, insuranceCompanyId from product, PAYMENT_PENDING default
5. EligibilityService: in-network, out-of-network, waiting period fail, limit exceeded
6. PolicyLimitService: reserve, commit, release, duplicate RESERVED fail
7. ClaimService: submit flow success, submit without item fail, submit not eligible fail
8. ClaimReviewService: approve success, reject success, invalid transition fail
9. PaymentService: complete activates policy, failed doesn't activate, refund completed payment
10. FraudScoringService: each rule individually, combined score
11. AiRiskAssessmentService: AI disabled fallback, timeout fallback
12. NotificationService: template render, mock send
13. OutboxEventService: save event, status transitions
14. ProcessedEventService: duplicate skip

Sonda: test siyahısı; test command-lar.
```

---

## Prompt 25 — Integration + E2E Tests (ƏVVƏLKİ 22-nin 2-ci hissəsi)

```text
SaglamOL üçün integration və E2E testləri yaz.

1. Integration tests (@SpringBootTest + @Testcontainers):

IAM: register → login → get me → refresh → logout (PostgreSQL container).

Policy: seed product → issue policy → eligibility check → reserve → commit (PostgreSQL + Redis containers).

Claim: submit claim → outbox event created → outbox worker publishes (PostgreSQL + Kafka containers).

2. Controller tests (MockMvc):
Hər service üçün: success, validation error (400), unauthorized (401), forbidden (403), not found (404), invalid state (409/422).

3. Docker smoke test:
scripts/docker-smoke-test.ps1:
- gradlew clean build
- gradlew bootJar
- docker compose config --quiet
- docker compose up -d --build
- docker compose ps
- health endpoint checks (curl hər service /actuator/health)

4. docs/testing.md: unit/integration/Docker smoke necə run edilir, Testcontainers requirements.

5. build.gradle: testImplementation 'org.testcontainers:testcontainers', 'org.testcontainers:postgresql', 'org.testcontainers:kafka'.

Sonda: test siyahısı; zəif test sahələri; test command-lar.
```

---

## Prompt 26 — Demo Data + Observability (ƏVVƏLKİ 23)

```text
Layihəni demo-ready vəziyyətə gətir.

1. Demo data (yalnız local profile, production-da avtomatik yaranmasın):

Demo user-lər (bcrypt hash, password: Test1234!):
- admin@saglamol.az → ADMIN
- insurance-admin@saglamol.az → INSURANCE_ADMIN
- insurance-staff@saglamol.az → INSURANCE_STAFF
- agent@saglamol.az → AGENT
- patient@saglamol.az → PATIENT
- doctor@saglamol.az → DOCTOR
- hospital-admin@saglamol.az → HOSPITAL_ADMIN
- hospital-staff@saglamol.az → HOSPITAL_STAFF

Demo business data: insurance company (ACTIVE), hospital + branch, doctor assignment, patient/doctor/agent profile, insurance product + coverage rules, provider contract, policy (ACTIVE), notification templates.

Variant: Liquibase context=local seed data.

2. Observability:
- Bütün service-lərdə /actuator/health,info,metrics,prometheus.
- Docker compose-a Prometheus + Grafana əlavə et.
- docker/prometheus/prometheus.yml — bütün service-ləri target et.
- Grafana basic dashboard JSON optional.

3. Logging: X-Correlation-Id bütün loglarda (MDC), sensitive data loglanmasın.

4. README: project overview, business model, multi-company architecture, roles, module list, ports, DBs, env vars, Docker run, local dev, Swagger links, Postman, Kafka topics, MinIO, AI setup, Prometheus/Grafana, demo flow, testing, known limitations, next phase roadmap.

5. docs/architecture.md: Mermaid C4 context/container, login sequence, company onboarding, hospital onboarding, policy purchase, claim submit, AI/fraud async, claim approve/payment/notification.

6. .env.example aktual saxla.

Sonda: demo flow; docker startup command-lar.
```

---

## Prompt 27 — Regression Fix (ƏVVƏLKİ 24-ün 1-ci hissəsi — SPESİFİK SCOPE)

```text
Layihədə compile error-lar, dependency conflict-lər, config uyğunsuzluqları düzəlt.

Spesifik yoxlama siyahısı:

1. Compile errors:
- gradlew clean build işlət. Hər module-u ayrıca yoxla.
- package/import səhvləri düzəlt.
- DTO/mapper uyğunsuzluqları düzəlt (field tip/ad dəyişiklikləri).
- Entity və Liquibase schema uyğunsuzluqları: field adları, tipləri, constraint-lər.

2. Config uyğunsuzluqları:
- API Gateway route-ları: bütün servis-lərə route var?
- Docker Compose: service adları, port-lar, depends_on, healthcheck-lər.
- Kafka topic adları: KafkaTopics sabitləri ilə consumer/producer uyğun?
- MinIO config: bucket adı, credentials.
- Redis config: host, port.
- Config Server: bütün service config-ləri mövcud?

3. Dependency conflicts:
- Spring Boot BOM versiyaları.
- spring-kafka versiyası.
- MapStruct + Lombok birlikdə işləyir?
- common-kafka modulu bütün service-lərdən access edilə bilir?

4. Liquibase:
- changeset naming convention ardıcıl.
- Rollback mümkün olan changeset-lərə rollback əlavə et.
- context=local demo data production-da işləməsin.

5. Docker validation:
- docker compose config --quiet keçməlidir.
- Hər service Dockerfile mövcud və düzgün.

6. .env.example: bütün lazımi variable-lar mövcud.

Final validation:
```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose config --quiet
```

Sonda: düzəltdiyin problemləri siyahıla; hansı module-lar uğurlu build oldu; qalan problemlər.
```

---

## Prompt 28 — Security + Production Hardening (ƏVVƏLKİ 24-ün 2-ci hissəsi)

```text
Mövcud platformanı təhlükəsiz və deploy-ready vəziyyətə gətir.

1. Security hardening:
- JWT secret hardcode olmasın — yalnız env variable.
- Internal service secret hardcode olmasın.
- Public endpoint-lər aydın ayrılsın (SecurityConfig-lərdə).
- Admin endpoint-lər qorunsun.
- Ownership AuthContext əsasında yoxlansın — request body userId/companyId-yə güvənmə.
- Password reset token loglarda maskalansın.
- CORS local/prod ayrımı.
- Gateway basic rate limiting əlavə et (əgər mümkündürsə).

2. Validation hardening:
- Bütün create/update DTO-lar @NotNull, @NotBlank, @Email, @Size, @Min, @Max annotation-lı.
- Money amount mənfi olmasın (@DecimalMin("0")).
- Date range validation (endDate >= startDate).
- Enum validation.
- Pagination max size: @Max(100).

3. Error handling standardization:
- Bütün service-lərdə ErrorResponse formatı: code, message, correlationId, timestamp, details(opt).
- HTTP status: 400, 401, 403, 404, 409, 422, 500.
- Stacktrace response-da görünməsin (production profile-da).
- correlationId error response-da qayıtsın.
- @ControllerAdvice GlobalExceptionHandler hər service-də.

4. Logging hardening:
- correlationId bütün loglarda (MDC yoxla).
- Sensitive data masking: email, phone, password hash, JWT token.
- AI payload PII minimization.
- Payment logs PII masking.

5. Access control tests (mütləq):
- patient cannot access other patient data → 403
- agent cannot access another insurance company data → 403
- insurance admin cannot access another company data → 403
- hospital admin cannot access another hospital data → 403
- doctor cannot access unassigned hospital data → 403
- unauthenticated request → 401
- insufficient role → 403

6. Known limitations README-də olsun:
- real payment provider yoxdur (mock)
- real SMS/email provider yoxdur (mock)
- AI sadə API key ilə işləyir
- OCR, FHIR/HL7 yoxdur
- Kubernetes yoxdur
- production secrets management yoxdur
- full load testing, security audit gələcək mərhələdir

Final validation:
```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose config --quiet
```

Əgər mümkündürsə:
```powershell
docker compose up -d --build
docker compose ps
```

Sonda: düzəltdiyin problemləri siyahıla; hansı testlər keçdi; production üçün qalan işlər; layihənin final vəziyyəti.
```

---

# Asılılıq Qrafiki

```
P1 Audit
├── P2 Config Server
├── P3 common-events
│   └── P4 Outbox/Kafka Infrastructure ← ERKƏN
│       ├── P5 InsuranceCompany Entity
│       │   └── P6 InsuranceCompany Service
│       │       └── P7 Company Ownership
│       │           └── P8 User Profile Final
│       │               ├── P9 Policy Schema (migration!)
│       │               │   ├── P10 Policy API Part 1
│       │               │   │   └── P11 Policy API Part 2
│       │               │   │       └── P12 Payment Service
│       │               │   │           └── P13 Claim Schema
│       │               │   │               ├── P14 Claim Lifecycle
│       │               │   │               │   └── P15 Claim Kafka/Feign
│       │               │   │               │       ├── P18 Fraud Detection
│       │               │   │               │       └── P19 AI Risk
│       │               │   └── P16 Health Record Schema
│       │               │       └── P17 Health Record MinIO
│       │               │           └── P22 Doc Hash Cross-check
│       │               └── P20 Notification
│       └── P21 Event Integration
│           └── P23 OpenAPI/Postman
│               ├── P24 Unit Tests
│               │   └── P25 Integration Tests
│               │       └── P26 Demo + Observability
│               │           └── P27 Regression Fix
│               │               └── P28 Hardening
```

# Parallel İcra İmkanları

Bəzi promptlar **paralel** icra oluna bilər (əgər 2 agent istifadə edirsənsə):

```
Paralel Qrup 1: P10 (Policy Products) ‖ P16 (Health Record Schema)
Paralel Qrup 2: P18 (Fraud) ‖ P19 (AI Risk)
Paralel Qrup 3: P20 (Notification) ‖ P22 (Doc Hash)
```

---

# Yekun Nəticə

28 prompt tamamlandıqdan sonra:

```text
✅ Backend-only professional MVP
✅ Multi-company e-health insurance platform
✅ Hospital-aware claim/payment/policy flow
✅ Demo-ready microservice backend
✅ Deploy-ready local Docker environment
✅ Vahid Outbox + Kafka infrastructure (erkən qurulub)
✅ InsurancePlan → InsuranceProduct migration düzgün aparılıb
✅ Bölünmüş promptlar — hər biri 1 sesiyaya sığır
✅ Regression və Hardening ayrıca, spesifik scope ilə
```
