# SaglamOL — Tam Deploy Plan: 32 Prompt

> Bu sənəd proyekti deploy vəziyyətinə çatdırmaq üçün addım-addım icra ediləcək 
> hazır promptlar toplusudur. Hər prompt ayrı söhbətdə (yaxud ardıcıl) göndərilir.
> **Sıra çox vacibdir** — asılılıqlar nəzərə alınıb.

---

## 📋 Tamamlanma İzləyici

- `[ ]` Gözlənilir
- `[/]` İcrada  
- `[x]` Tamamdı

---

## 🔵 QRUP A — İnfrastruktur (2 prompt)

### PROMPT A-1: Config Server konfiqurasiyası
- `[ ]` **A-1**

```
SaglamOL layihəsinin config-server-i Spring Cloud Config Server native modunda 
işləyir (classpath:/config). config-server aşağıdakı servislərin konfiqurasiya 
fayllarını saxlamalıdır:

Aşağıdakı servislərin hər biri üçün 
infrastructure/config-server/src/main/resources/config/ qovluğunda 
{servis-adı}.yml faylı yarat:

- user-profile-service.yml
- policy-service.yml
- claim-service.yml
- health-record-service.yml
- ai-risk-service.yml
- fraud-detection-service.yml
- notification-service.yml
- payment-service.yml

Hər faylda servis-spesifik konfiqurasiya olsun. Hər servis öz application.yml-dən 
konfiq server-dən override edə bilər. Yalnız config-server-in Dockerfile-ını da 
nəzərdən keçir və lazım gələrsə düzəlt.

Mövcud application.yml-ləri sil, yalnız minimal self-config saxla. 
IAM service konfig serverindən istifadə etməyəcək (artıq tamam konfiqurəlidir).
```

---

### PROMPT A-2: common-events Kafka event sinifləri
- `[ ]` **A-2**

```
SaglamOL layihəsinin common/common-events modulunu tamamla.

Hazırda yalnız EventEnvelope.java var. Aşağıdakı event siniflərini 
az.saglamol.common.events paketi altında yarat:

IAM events:
- UserRegisteredEvent(userId, email, phoneNumber, roles, occurredAt)

Policy events:
- PolicyIssuedEvent(policyId, patientId, planId, startDate, endDate, occurredAt)
- PolicyCancelledEvent(policyId, patientId, reason, occurredAt)
- PolicyLimitReservedEvent(reservationId, policyId, claimId, serviceType, reservedAmount, occurredAt)
- PolicyLimitConfirmedEvent(reservationId, policyId, claimId, occurredAt)
- PolicyLimitReleasedEvent(reservationId, policyId, claimId, reason, occurredAt)

Claim events:
- ClaimSubmittedEvent(claimId, patientId, policyId, claimType, totalAmount, items, occurredAt)
- ClaimStatusChangedEvent(claimId, patientId, oldStatus, newStatus, reason, occurredAt)
- ClaimApprovedEvent(claimId, patientId, policyId, approvedAmount, decidedBy, occurredAt)
- ClaimRejectedEvent(claimId, patientId, reason, decidedBy, occurredAt)

AI Risk events:
- RiskAnalysisRequestedEvent(claimId, patientId, policyId, claimType, totalAmount, occurredAt)
- RiskAnalysisCompletedEvent(claimId, riskAnalysisId, score, level, confidence, reasons, occurredAt)

Fraud events:
- FraudCheckRequestedEvent(claimId, patientId, policyId, totalAmount, occurredAt)
- FraudCheckCompletedEvent(claimId, fraudCheckId, fraudScore, signals, passed, occurredAt)

Notification events:
- NotificationRequestedEvent(recipientUserId, channel, templateCode, variables, occurredAt)

Payment events:
- PaymentCompletedEvent(transactionId, policyId, patientId, amount, occurredAt)
- ClaimPayoutRequestedEvent(claimId, patientId, amount, occurredAt)
- ClaimPayoutCompletedEvent(claimId, transactionId, paidAmount, occurredAt)
- ClaimPayoutFailedEvent(claimId, reason, occurredAt)

Health Record events:
- HealthRecordCreatedEvent(healthRecordId, patientId, doctorId, occurredAt)
- MedicalDocumentUploadedEvent(documentId, healthRecordId, patientId, occurredAt)

Bütün event-lər Java record olsun. EventEnvelope-i event-ləri sarmaq üçün 
generic tip parametri əlavə et: EventEnvelope<T>.

Kafka topic sabitlərini ayrıca KafkaTopics sinifində saxla:
az.saglamol.common.events.KafkaTopics

common-events/build.gradle-a kafka dependency əlavə et.
```

---

## 🟢 QRUP B — User Profile Service (2 prompt)

### PROMPT B-1: User Profile Service — Liquibase, Repository, Mapper
- `[ ]` **B-1**

```
SaglamOL layihəsinin user-profile-service modulunu tamamla.

Entity-lər artıq mövcuddur: PatientProfile, DoctorProfile, AgentProfile, 
Hospital, HospitalBranch, HospitalStaffProfile, DoctorHospitalAssignment, Address (Embeddable).

Aşağıdakıları yarat/tamamla:

1. Liquibase:
   src/main/resources/db/changelog/db.changelog-master.xml — master changelog
   src/main/resources/db/changelog/migrations/001-create-user-profile-tables.xml
   
   Cədvəllər: patient_profile, doctor_profile, agent_profile, address (embedded),
   hospital, hospital_branch, hospital_staff_profile, doctor_hospital_assignment
   
   Seed: heç bir data lazım deyil.

2. Repository-lər:
   - PatientProfileRepository (findByIamUserId, search by name/email)
   - DoctorProfileRepository (findByIamUserId, findByLicenseNumber, search)
   - AgentProfileRepository (findByIamUserId, findByEmployeeCode, search)
   - HospitalRepository (findByRegistrationNumber)
   - HospitalBranchRepository (findAllByHospitalId)
   - HospitalStaffProfileRepository (findByIamUserIdAndHospitalId)
   - DoctorHospitalAssignmentRepository (findByDoctorProfileIdAndHospitalId)

3. DTO-lar: CreatePatientProfileRequest, UpdatePatientProfileRequest, PatientProfileResponse,
   CreateDoctorProfileRequest, UpdateDoctorProfileRequest, DoctorProfileResponse,
   CreateAgentProfileRequest, AgentProfileResponse,
   CreateHospitalRequest, HospitalResponse, CreateBranchRequest, BranchResponse

4. MapStruct Mapper-lar (UserProfileMapper, HospitalMapper)

5. application.yml-ə liquibase konfiqurasiyasını əlavə et
```

---

### PROMPT B-2: User Profile Service — Service Layer, Controller, Security
- `[ ]` **B-2**

```
SaglamOL user-profile-service modulunun service layer-ini tamamla.
Entity-lər, repository-lər, DTO-lar, mapper-lər artıq mövcuddur.

UserProfileService.java faylı mövcuddur amma boşdur. Aşağıdakıları implement et:

UserProfileService metodları:
- createPatientProfile(AuthContext, CreatePatientProfileRequest) → PatientProfileResponse
- getMyPatientProfile(AuthContext) → PatientProfileResponse
- updatePatientProfile(AuthContext, UpdatePatientProfileRequest) → PatientProfileResponse
- createDoctorProfile(AuthContext, CreateDoctorProfileRequest) → DoctorProfileResponse
- getMyDoctorProfile(AuthContext) → DoctorProfileResponse
- updateDoctorProfile(AuthContext, UpdateDoctorProfileRequest) → DoctorProfileResponse
- createAgentProfile(AuthContext, CreateAgentProfileRequest) → AgentProfileResponse
- getMyAgentProfile(AuthContext) → AgentProfileResponse
- searchPatients(query, status, pageable) → Page<PatientProfileResponse> [ADMIN, AGENT]
- searchDoctors(query, status, pageable) → Page<DoctorProfileResponse> [ADMIN, AGENT, HOSPITAL_ADMIN]
- getPatientProfileById(UUID profileId, AuthContext) → PatientProfileResponse

HospitalService.java mövcuddur amma boşdur. Implement et:
- createHospital(AuthContext, CreateHospitalRequest) → HospitalResponse [ADMIN]
- getHospital(hospitalId) → HospitalResponse
- getAllHospitals(pageable) → Page<HospitalResponse>
- createBranch(AuthContext, hospitalId, CreateBranchRequest) → BranchResponse [HOSPITAL_ADMIN, ADMIN]
- assignDoctor(AuthContext, hospitalId, doctorProfileId) → void [HOSPITAL_ADMIN, ADMIN]

Authorization: docs/provider-model.md-dəki qaydalara uyğun ownership yoxlaması.
AuthContext-i common-security-dən istifadə et (InternalAuthFilter artıq işləyir).

Controller-ləri də tamamla: UserProfileController, HospitalController
Hər endpoint üçün swagger annotation əlavə et.
Exception handling: UserProfileException sinfi yarat.
```

---

## 🟡 QRUP C — Policy Service (3 prompt)

### PROMPT C-1: Policy Service — Liquibase, Repository, DTO, Mapper
- `[ ]` **C-1**

```
SaglamOL layihəsinin policy-service modulunu tamamla.

Entity-lər mövcuddur: InsurancePlan, CoverageRule, Policy, PolicyLimitUsage, PolicyLimitReservation.
Entity field-ları minimal tutulub — genişlət:

InsurancePlan: id, code, name, description, status (ACTIVE/INACTIVE), monthlyPrice, createdAt, updatedAt
CoverageRule: id, planId, serviceType (HOSPITAL/DENTAL/OPTICAL/MENTAL_HEALTH/PHARMACY/LAB), 
              coveragePercent, annualLimit, waitingPeriodDays, createdAt
Policy: id, patientId, planId, status (PENDING/ACTIVE/EXPIRED/CANCELLED), 
        startDate, endDate, createdAt, updatedAt
PolicyLimitUsage: id, policyId, serviceType, annualLimit, usedAmount, reservedAmount, @Version
PolicyLimitReservation: id, policyId, claimId, serviceType, reservedAmount, 
                        status (PENDING/CONFIRMED/RELEASED), createdAt, updatedAt

Yaratdıqlarını:
1. Liquibase changelog: 001-create-policy-tables.xml + seed: insurance_plan cədvəlinə 
   3 plan əlavə et (BASIC, STANDARD, PREMIUM)
2. Repository-lər: InsurancePlanRepository, PolicyRepository (findAllByPatientId, findActiveByPatientId),
   PolicyLimitUsageRepository, PolicyLimitReservationRepository, CoverageRuleRepository
3. DTO-lar: CreateInsurancePlanRequest, InsurancePlanResponse, CreateCoverageRuleRequest,
   CoverageRuleResponse, IssuePolicyRequest(patientId, planId, startDate), PolicyResponse,
   PolicyEligibilityResponse(eligible, reason, availableLimit)
4. MapStruct Mapper-lar
5. policy-service/build.gradle-a spring-data-redis, spring-cache dependency əlavə et
6. application.yml-ə liquibase + redis konfiqurasiyası əlavə et
```

---

### PROMPT C-2: Policy Service — Service Layer, Controller
- `[ ]` **C-2**

```
SaglamOL policy-service modulunun service layer-ini yarat.
Entity-lər, repository-lər, DTO-lar hazırdır.

Aşağıdakı sinifləri yarat:

InsurancePlanService:
- createPlan(CreateInsurancePlanRequest) → InsurancePlanResponse [ADMIN]
- getAllPlans(status, pageable) → Page<InsurancePlanResponse>
- getPlanById(planId) → InsurancePlanResponse
- addCoverageRule(planId, CreateCoverageRuleRequest) → CoverageRuleResponse [ADMIN]
- getCoverageRules(planId) → List<CoverageRuleResponse>

PolicyService:
- issuePolicy(AuthContext, IssuePolicyRequest) → PolicyResponse [AGENT, ADMIN]
- getMyPolicies(AuthContext) → List<PolicyResponse> [PATIENT]
- getPolicyById(policyId, AuthContext) → PolicyResponse
- cancelPolicy(policyId, AuthContext, reason) → PolicyResponse [AGENT, ADMIN]
- checkEligibility(policyId, serviceType, requestedAmount) → PolicyEligibilityResponse

PolicyLimitService (claim-service tərəfindən çağırılacaq):
- reserveLimit(policyId, claimId, serviceType, amount) → PolicyLimitReservation
  (optimistic lock retry ilə — @Retryable və ya manual retry)
- confirmReservation(reservationId) → void
- releaseReservation(reservationId, reason) → void
- getUsageSummary(policyId) → Map<serviceType, LimitUsageSummary>

@Cacheable(Redis) ilə InsurancePlan, CoverageRule məlumatlarını keşlə (TTL: 10 dəq).

PolicyController yarat: /api/v1/policies
InsurancePlanController yarat: /api/v1/insurance-plans
AuthContext-i common-security-dən istifadə et.
PolicyException sinfi yarat.
Swagger annotation-lar əlavə et.
```

---

### PROMPT C-3: Policy Service — Internal API endpoint (claim-service üçün)
- `[ ]` **C-3**

```
SaglamOL policy-service moduluna daxili servis-arası API əlavə et.

Claim-service, eligibility check və limit rezervasiyası üçün policy-service-ə 
müraciət edəcək. Bunlar daxili API-lardır (Gateway-dən keçmir, Eureka üzərindən 
birbaşa çağırılır).

PolicyInternalController yarat (/internal/v1/policies):
- GET /internal/v1/policies/{policyId}/eligibility?serviceType=HOSPITAL&amount=1500.00
  → PolicyEligibilityResponse
- POST /internal/v1/policies/{policyId}/limit-reservations
  → body: {claimId, serviceType, amount}
  → PolicyLimitReservationResponse
- PUT /internal/v1/policies/limit-reservations/{reservationId}/confirm
  → 200 OK
- PUT /internal/v1/policies/limit-reservations/{reservationId}/release
  → body: {reason}
  → 200 OK
- GET /internal/v1/policies/{policyId}/active
  → PolicyResponse (aktiv policy varmı? claimId validation üçün)

Bu endpoint-lər JWT lazım deyil — yalnız daxili şəbəkə üçündür.
SecurityConfig-ə /internal/** paths-ını permit et.

Feign Client interfeysi yarat: PolicyInternalClient
Bu client claim-service, ai-risk-service, fraud-detection-service 
tərəfindən istifadə ediləcək.
Bunu common-events moduluna yox, hər servisə ayrıca kopyala (Feign client 
interface code duplication qəbul edilir bu arxitekturada).
```

---

## 🟠 QRUP D — Claim Service (3 prompt)

### PROMPT D-1: Claim Service — Entity genişlətmə, Liquibase, Repository, DTO
- `[ ]` **D-1**

```
SaglamOL claim-service modulunu tamamla.

Entity-lər mövcuddur: Claim, ClaimItem, ClaimDecision, ClaimStatusHistory, 
OutboxEvent, ConsumerProcessedEvent.

Entity field-larını genişlət:

Claim: id, patientId, policyId, status (DRAFT/SUBMITTED/UNDER_REVIEW/APPROVED/REJECTED/
       PAYMENT_PENDING/PAID/PAYOUT_FAILED/CANCELLED), claimType (HOSPITAL/DENTAL/OPTICAL/
       MENTAL_HEALTH/PHARMACY/LAB), totalAmount, submittedAt, updatedAt, createdAt,
       riskScore (BigDecimal nullable), riskLevel (String nullable),
       fraudScore (BigDecimal nullable), fraudPassed (Boolean nullable)

ClaimItem: id, claimId, serviceType, description, amount, serviceDate, providerId (nullable)

ClaimDecision: id, claimId, decision (APPROVED/REJECTED), decidedBy (UUID), 
               reason, approvedAmount (nullable), createdAt

ClaimStatusHistory: id, claimId, oldStatus, newStatus, changedAt, changedBy (UUID nullable), note

OutboxEvent: mövcud field-lər saxla (artıq tam)

ConsumerProcessedEvent: id, topic, partitionId, offsetValue, processedAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-claim-tables.xml
2. Repository-lər: ClaimRepository (findAllByPatientId, findByIdAndPatientId),
   ClaimItemRepository, ClaimDecisionRepository, ClaimStatusHistoryRepository,
   OutboxEventRepository (findByStatusAndNextRetryAtBeforeOrderByCreatedAt),
   ConsumerProcessedEventRepository (existsByTopicAndPartitionIdAndOffsetValue)
3. DTO-lar: CreateClaimRequest, ClaimItemRequest, ClaimResponse, 
   ClaimSummaryResponse, ReviewClaimRequest, ClaimDecisionResponse
4. MapStruct Mapper
5. claim-service/build.gradle-a spring-cloud-starter-openfeign əlavə et
```

---

### PROMPT D-2: Claim Service — Service Layer, State Machine, OutboxWorker
- `[ ]` **D-2**

```
SaglamOL claim-service modulunun service layer-ini yarat.
Entity-lər, repository-lər, DTO-lar hazırdır.

ClaimService yarat:
- submitClaim(AuthContext, CreateClaimRequest) → ClaimResponse
  Axın:
  1. PatientId-ni AuthContext-dən götür (yalnız PATIENT rolu)
  2. policy-service-dən eligibility yoxla (PolicyInternalClient — Feign)
  3. Limit rezerv et (policy-service limit-reservations)
  4. Claim DB-ə SUBMITTED status ilə yaz
  5. ClaimStatusHistory yaz (null → SUBMITTED)
  6. OutboxEvent DB-ə yaz (ClaimSubmittedEvent) — eyni @Transactional
  
- getMyClaimsHistory(AuthContext, pageable) → Page<ClaimSummaryResponse> [PATIENT]
- getClaimById(claimId, AuthContext) → ClaimResponse
- getClaimsByStatus(status, pageable) → Page<ClaimSummaryResponse> [AGENT, ADMIN]

ClaimReviewService yarat:
- startReview(claimId, AuthContext) → ClaimResponse [AGENT, ADMIN]
  (SUBMITTED → UNDER_REVIEW)
- approveClaim(claimId, AuthContext, ReviewClaimRequest) → ClaimResponse [AGENT, ADMIN]
  1. Status: UNDER_REVIEW → APPROVED
  2. ClaimDecision yaz
  3. Policy limit-ini confirm et (PolicyInternalClient)
  4. OutboxEvent yaz (ClaimApprovedEvent + ClaimPayoutRequestedEvent)
- rejectClaim(claimId, AuthContext, ReviewClaimRequest) → ClaimResponse [AGENT, ADMIN]
  1. Status: UNDER_REVIEW → REJECTED
  2. ClaimDecision yaz
  3. Policy limit rezervasiyasını release et
  4. OutboxEvent yaz (ClaimRejectedEvent)

Kafka Consumers:
- RiskAnalysisConsumer: RiskAnalysisCompletedEvent → Claim.riskScore, riskLevel yenilə
  (idempotency: ConsumerProcessedEvent yoxla)
- FraudCheckConsumer: FraudCheckCompletedEvent → Claim.fraudScore, fraudPassed yenilə
  (idempotency: ConsumerProcessedEvent yoxla)
- PaymentConsumer: ClaimPayoutCompletedEvent → PAYMENT_PENDING → PAID
                   ClaimPayoutFailedEvent → PAYOUT_FAILED

OutboxWorker (@Scheduled, hər 5 saniyə):
- PENDING OutboxEvent-ləri oxu
- Kafka-ya publish et (EventEnvelope<T>)
- status=PUBLISHED, publishedAt=now()
- Xəta baş verərsə: retryCount++, nextRetryAt=now+30s, lastError=message

ClaimController yarat: /api/v1/claims
Swagger annotation-lar əlavə et.
ClaimException sinfi yarat.
```

---

### PROMPT D-3: Claim Service — Kafka Config, Feign Config
- `[ ]` **D-3**

```
SaglamOL claim-service moduluna Kafka producer/consumer və Feign client 
konfiqurasiyasını əlavə et.

1. KafkaProducerConfig: 
   - key: String, value: serializer JSON
   - Topic-lər: KafkaTopics sabitlərindən istifadə et (common-events)
   
2. KafkaConsumerConfig:
   - Group id: claim-service-group
   - Topics: risk.analysis.completed, fraud.check.completed, payment.payout.completed, payment.payout.failed
   - Deserializer: JsonDeserializer
   - Manual acknowledge (idempotency üçün)

3. FeignConfig:
   - claim-service-in digər servislərə (policy-service) çağırışları üçün
   - Eureka discovery ilə (spring.application.name ilə)
   - Timeout: connect=2s, read=5s

4. application.yml-ə əlavə et:
   - kafka config
   - feign config
   - scheduling: @EnableScheduling

5. claim-service/build.gradle-a əlavə et:
   - spring-kafka
   - spring-cloud-starter-openfeign

KafkaTopics sinifindən istifadə et (common-events-də artıq yaradıldı).
```

---

## 🔵 QRUP E — Health Record Service (2 prompt)

### PROMPT E-1: Health Record Service — Entity, Liquibase, Repository, DTO
- `[ ]` **E-1**

```
SaglamOL health-record-service modulunu tamamla.

Entity-lər mövcuddur: HealthRecord, Treatment, MedicalDocument, DocumentHashIndex, HealthAccessLog.

Field-ları genişlət:

HealthRecord: id, patientId, doctorId (nullable), hospitalId (nullable), 
              visitDate, visitType (INPATIENT/OUTPATIENT/EMERGENCY), 
              diagnosis, notes, status (ACTIVE/ARCHIVED), createdAt, updatedAt

Treatment: id, healthRecordId, treatmentType, description, 
           startDate, endDate, medications, createdAt

MedicalDocument: id, healthRecordId, patientId, documentType (LAB_RESULT/PRESCRIPTION/
                 IMAGING/DISCHARGE_SUMMARY/OTHER), fileName, fileSize, contentType,
                 minioKey, status (PENDING_UPLOAD/CONFIRMED/UPLOAD_FAILED), 
                 sha256Hash, createdAt, updatedAt

DocumentHashIndex: id, sha256Hash, documentId, patientId, createdAt (fraud üçün)

HealthAccessLog: id, healthRecordId, accessedByUserId, accessRole, accessedAt, reason

Yaratdıqlarını:
1. Liquibase: 001-create-health-record-tables.xml
2. Repository-lər: HealthRecordRepository, TreatmentRepository, 
   MedicalDocumentRepository (findByMinioKey, findBysha256Hash),
   DocumentHashIndexRepository (existsBysha256HashAndPatientId),
   HealthAccessLogRepository
3. DTO-lar: CreateHealthRecordRequest, HealthRecordResponse, CreateTreatmentRequest,
   TreatmentResponse, InitiateDocumentUploadRequest, InitiateDocumentUploadResponse,
   ConfirmDocumentUploadRequest, MedicalDocumentResponse
4. MapStruct Mapper
5. health-record-service/build.gradle-a əlavə et: spring-kafka, 
   io.minio:minio:8.5.7 (MinIO Java SDK)
6. application.yml-ə MinIO konfigurasiyası əlavə et
```

---

### PROMPT E-2: Health Record Service — MinIO Service Layer, Controller
- `[ ]` **E-2**

```
SaglamOL health-record-service modulunun service layer-ini yarat.

MinioStorageService yarat:
- generatePresignedUploadUrl(bucketName, objectKey, expiryMinutes) → String URL
- generatePresignedDownloadUrl(bucketName, objectKey, expiryMinutes) → String URL
- deleteObject(bucketName, objectKey) → void
- ensureBucketExists(bucketName) → void
- Bucket adı: medical-documents

HealthRecordService yarat:
- createHealthRecord(AuthContext, CreateHealthRecordRequest) → HealthRecordResponse [DOCTOR, HOSPITAL_STAFF]
- getMyHealthRecords(AuthContext, pageable) → Page<HealthRecordResponse> [PATIENT]
- getHealthRecord(healthRecordId, AuthContext) → HealthRecordResponse (access log yaz)
- addTreatment(healthRecordId, AuthContext, CreateTreatmentRequest) → TreatmentResponse

MedicalDocumentService yarat:
- initiateUpload(AuthContext, healthRecordId, InitiateDocumentUploadRequest) → InitiateDocumentUploadResponse
  1. MedicalDocument yaz (status=PENDING_UPLOAD)
  2. MinIO presigned PUT URL yarat (15 dəq)
  3. URL-i response-da qaytar (client birbaşa MinIO-ya upload edir)
  
- confirmUpload(AuthContext, documentId, ConfirmDocumentUploadRequest) → MedicalDocumentResponse
  1. Client sha256Hash göndərir
  2. Document status=CONFIRMED yenilə
  3. DocumentHashIndex yaz (fraud detection üçün)
  4. Kafka-ya MedicalDocumentUploadedEvent göndər

- getDocument(documentId, AuthContext) → presigned download URL (5 dəq) + metadata

HealthRecordController yarat: /api/v1/health-records
MedicalDocumentController yarat: /api/v1/health-records/{healthRecordId}/documents
Kafka producer config əlavə et.
Swagger annotation-lar.
HealthRecordException sinfi yarat.
```

---

## 🔴 QRUP F — AI Risk, Fraud, Notification, Payment (5 prompt)

### PROMPT F-1: AI Risk Service — OpenAI inteqrasiyası, Service Layer
- `[ ]` **F-1**

```
SaglamOL ai-risk-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: RiskAnalysis, RiskReason, AiRequestLog.
Field-ları genişlət:

RiskAnalysis: id, claimId, patientId, policyId, score (BigDecimal 0-1), 
              level (LOW/MEDIUM/HIGH/CRITICAL), confidence (BigDecimal),
              rawPrompt (Text), rawResponse (Text), status (PENDING/COMPLETED/FAILED),
              processingTimeMs, createdAt, completedAt

RiskReason: id, riskAnalysisId, category, description, weight

AiRequestLog: id, riskAnalysisId, model, inputTokens, outputTokens, 
              costUsd (nullable), latencyMs, status (SUCCESS/FAILED/TIMEOUT), createdAt

Yaratdıqlarını:
1. Liquibase: 001-create-ai-risk-tables.xml
2. Repository-lər: RiskAnalysisRepository, RiskReasonRepository, AiRequestLogRepository
3. DTO-lar: RiskAnalysisResponse, RiskReasonResponse
4. AiRiskModelClient interface (provayderə abstraksiya):
   - analyzeRisk(claimContext) → RiskAnalysisResult
5. OpenAiRiskModelClient implements AiRiskModelClient:
   - Spring RestClient ilə OpenAI /chat/completions endpoint-ə POST
   - Model: ${AI_MODEL:gpt-4o-mini}
   - System prompt: "You are a health insurance risk analyst..."
   - Timeout: 30 saniyə
   - Xəta halında: RiskAnalysis.status=FAILED
6. AiRiskService:
   - analyzeClaimRisk(ClaimSubmittedEvent) → void (Kafka consumer)
   - 1. RiskAnalysis PENDING yarat
   - 2. OpenAI çağır (AiRequestLog yaz)  
   - 3. RiskAnalysis COMPLETED/FAILED yenilə
   - 4. RiskAnalysisCompletedEvent Kafka-ya publish et
   - 5. ConsumerProcessedEvent yoxla (idempotency)
7. KafkaConsumerConfig (topic: claim.submitted, group: ai-risk-service-group)
8. KafkaProducerConfig (topic: risk.analysis.completed)
9. ai-risk-service/build.gradle-a spring-kafka əlavə et
10. application.yml: kafka, AI API key/url/model konfig
11. Controller: GET /api/v1/risk-analyses/{claimId} [AGENT, ADMIN]
```

---

### PROMPT F-2: Fraud Detection Service — Rule Engine, Service Layer
- `[ ]` **F-2**

```
SaglamOL fraud-detection-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: FraudCheck, FraudSignal, DocumentHashIndex.
Field-ları genişlət:

FraudCheck: id, claimId, patientId, policyId, fraudScore (BigDecimal 0-1),
            passed (boolean), status (PENDING/COMPLETED/FAILED), 
            signalsCount, createdAt, completedAt

FraudSignal: id, fraudCheckId, signalType (DUPLICATE_DOCUMENT/FREQUENT_CLAIMS/
             HIGH_AMOUNT_ANOMALY/SUSPICIOUS_TIMING/WAITING_PERIOD_VIOLATION),
             description, weight (BigDecimal), createdAt

DocumentHashIndex: id, sha256Hash, claimId, patientId, createdAt

Yaratdıqlarını:
1. Liquibase: 001-create-fraud-detection-tables.xml
2. Repository-lər: FraudCheckRepository, FraudSignalRepository, 
   DocumentHashIndexRepository (existsBysha256HashAndPatientIdNot)
3. FraudRule interface:
   evaluate(FraudCheckContext context) → Optional<FraudSignal>

4. Fraud Rules (hər biri ayrı sinif):
   - DuplicateDocumentRule: DocumentHashIndex-dən eyni hash başqa patient-də varmı?
   - FrequentClaimsRule: Son 30 gündə eyni patient-dən 3+ claim?
   - HighAmountAnomalyRule: Claim məbləği ortalamadan 3x çoxdursa?
   - WaitingPeriodRule: Policy-service-dən alınan coverageRule.waitingPeriodDays keçibmi?
                        (policy-service internal API-dan al)
   - SuspiciousTimingRule: Policy alınandan 7 gün içindədir?

5. FraudDetectionService (Kafka consumer: claim.submitted):
   - checkFraud(ClaimSubmittedEvent) → void
   - 1. FraudCheck PENDING yarat
   - 2. Bütün rule-ları işlət (FraudSignal-ları toplay)
   - 3. Score hesabla: Σ(signal.weight)
   - 4. passed = (score < 0.6)
   - 5. FraudCheck COMPLETED yenilə
   - 6. FraudCheckCompletedEvent Kafka-ya publish et
   - 7. idempotency: ConsumerProcessedEvent yoxla

6. KafkaConsumerConfig + KafkaProducerConfig
7. fraud-detection-service/build.gradle-a spring-kafka, openfeign əlavə et
8. application.yml konfiq
9. Controller: GET /api/v1/fraud-checks/{claimId} [AGENT, ADMIN]
```

---

### PROMPT F-3: Notification Service — Template, Kafka Consumer, Mock Sender
- `[ ]` **F-3**

```
SaglamOL notification-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: Notification, NotificationTemplate, NotificationRetry.
Field-ları genişlət:

NotificationTemplate: id, code (CLAIM_SUBMITTED/CLAIM_APPROVED/CLAIM_REJECTED/
                      POLICY_ISSUED/PASSWORD_RESET/WELCOME), channel (EMAIL/SMS/PUSH),
                      subject (nullable), bodyTemplate (text, Mustache format),
                      active, createdAt

Notification: id, recipientUserId, channel, templateCode, subject, body,
              status (PENDING/SENT/FAILED/PERMANENTLY_FAILED), 
              retryCount, maxRetries, sentAt, createdAt, updatedAt

NotificationRetry: id, notificationId, attemptNumber, failureReason, attemptedAt

Yaratdıqlarını:
1. Liquibase: 001-create-notification-tables.xml + 002-seed-templates.xml
   Seed: bütün template kodları üçün Azərbaycan dilində şablon mətnlər

2. Repository-lər: NotificationRepository, NotificationTemplateRepository, 
   NotificationRetryRepository
   
3. NotificationSender interface:
   - send(Notification notification) → boolean

4. MockEmailSender implements NotificationSender (channel=EMAIL):
   - LOGGER.info("📧 EMAIL SENT to userId={} subject={}...", ...)
   - Həmişə true qaytarır (real SMTP sonra əlavə olunacaq)

5. MockSmsSender implements NotificationSender (channel=SMS):
   - LOGGER.info("📱 SMS SENT to userId={} body={}...", ...)
   - Həmişə true qaytarır

6. TemplateRenderer: Mustache-dən istifadə et (body template + variables map)

7. NotificationService:
   - sendNotification(NotificationRequestedEvent) → void
   - createAndSend(recipientId, templateCode, variables, channel) → void

8. Kafka Consumers (group: notification-service-group):
   - ClaimApprovedEvent → "claim.approved" topic
   - ClaimRejectedEvent → "claim.rejected" topic
   - PolicyIssuedEvent → "policy.issued" topic
   - NotificationRequestedEvent → "notification.requested" topic

9. RetryScheduler (@Scheduled hər 1 dəq):
   FAILED status, retryCount < maxRetries → yenidən cəhd et
   retryCount >= maxRetries → PERMANENTLY_FAILED

10. KafkaConsumerConfig, application.yml, build.gradle (spring-kafka, mustache)
11. Controller: GET /api/v1/notifications/my [PATIENT, DOCTOR, AGENT]
```

---

### PROMPT F-4: Payment Service — Transaction Tracking, Kafka
- `[ ]` **F-4**

```
SaglamOL payment-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: PaymentTransaction, PaymentEventLog.
Field-ları genişlət:

PaymentTransaction: id, type (PREMIUM_PAYMENT/CLAIM_PAYOUT/REFUND), 
                    relatedEntityId (policyId və ya claimId), patientId,
                    amount, currency (default: AZN), status (PENDING/COMPLETED/FAILED/REFUNDED),
                    externalTransactionId (nullable — payment gateway ref),
                    failureReason (nullable), initiatedAt, completedAt, updatedAt

PaymentEventLog: id, transactionId, eventType, eventData (JSON text), occurredAt

Yaratdıqlarını:
1. Liquibase: 001-create-payment-tables.xml

2. Repository-lər: PaymentTransactionRepository, PaymentEventLogRepository

3. PaymentGatewayClient interface:
   - initiatePayment(amount, currency, metadata) → GatewayPaymentResult

4. MockPaymentGatewayClient implements PaymentGatewayClient:
   - LOGGER.info("💳 MOCK PAYMENT processed amount={} currency={}", ...)
   - Həmişə uğurlu nəticə qaytarır (externalId = UUID.randomUUID())
   - 5% ehtimalla failure simulyasiyası (random)

5. PaymentService:
   - processPremiumPayment(policyId, patientId, amount) → PaymentTransaction [PATIENT]
   - processClaimPayout(claimId, patientId, amount) → PaymentTransaction (internal)

6. Kafka Consumers (group: payment-service-group):
   - ClaimPayoutRequestedEvent → "claim.payout.requested" topic:
     1. PaymentTransaction PENDING yarat
     2. MockPaymentGatewayClient çağır
     3. Transaction COMPLETED/FAILED yenilə
     4. ClaimPayoutCompletedEvent və ya ClaimPayoutFailedEvent publish et

7. KafkaConsumerConfig + KafkaProducerConfig
8. application.yml, build.gradle (spring-kafka)
9. Controller: GET /api/v1/payments/my [PATIENT] — öz ödəniş tarixçəsi
10. Controller: GET /api/v1/payments/{transactionId} [AGENT, ADMIN]
```

---

### PROMPT F-5: Fraud + Health Record — DocumentHash cross-check
- `[ ]` **F-5**

```
SaglamOL-da health-record-service və fraud-detection-service arasında 
dokument duplikatı yoxlaması üçün inteqrasiya əlavə et.

Problem: Xəstə eyni tibbi sənədi (məsələn, bir analizin şəklini) bir neçə 
claim-ə əlavə edə bilər. Fraud detection bu halı aşkarlamalıdır.

Həll:
1. health-record-service-in MedicalDocumentService.confirmUpload() metodunda
   artıq MedicalDocument.sha256Hash saxlanır.
   Bunu claim-service ilə paylaşmaq üçün ClaimItemRequest-ə 
   documentId (nullable UUID) field əlavə et.

2. claim-service-in submitClaim() metodunda:
   Hər ClaimItem üçün documentId varsa, onu ClaimSubmittedEvent-ə daxil et.

3. fraud-detection-service-in DuplicateDocumentRule-unda:
   ClaimSubmittedEvent-dəki document hash-lərini (health-record-service 
   internal API-dan al) öz DocumentHashIndex-i ilə müqayisə et.

4. HealthRecordInternalController yarat (/internal/v1/health-records):
   - GET /internal/v1/documents/{documentId}/hash → {sha256Hash, patientId}
   
5. fraud-detection-service-ə HealthRecordInternalClient (Feign) əlavə et.

6. Fraud detection-da DocumentHashIndex:
   - ClaimSubmittedEvent gəldiyi zaman, əgər document-lər varsa,
     fraud-check bitdikdən sonra confirmed hash-ləri öz DocumentHashIndex-inə yaz.
```

---

## 🟣 QRUP G — Test və Deploy (4 prompt)

### PROMPT G-1: IAM Service — Unit Tests
- `[ ]` **G-1**

```
SaglamOL iam-service üçün JUnit 5 + Mockito unit testlər yaz.

Test sinifləri:

IamApplicationServiceTest:
- register_success: yeni user yaradılır, PATIENT rolu alır
- register_emailAlreadyExists_throws: EMAIL_ALREADY_EXISTS xətası
- register_phoneAlreadyExists_throws: PHONE_ALREADY_EXISTS xətası
- loginWithEmail_success: aktiv user, düzgün şifrə → token qaytarır
- loginWithEmail_wrongPassword_throws: INVALID_CREDENTIALS xətası
- loginWithEmail_inactiveUser_throws: INVALID_CREDENTIALS xətası
- loginWithPhone_success
- refresh_success: valid token → rotate
- refresh_revokedToken_throws: REFRESH_TOKEN_REUSED, family revoke
- logout_success

RefreshTokenServiceTest:
- issue_success: token yaradılır, hash saxlanılır
- rotate_success: köhnə revoke, yenisi yaradılır
- rotate_expiredToken_throws
- rotate_revokedToken_revokesFamily: bütün family revoke olunur

PasswordServiceTest:
- changePassword_success: şifrə dəyişir, aktiv tokenlar revoke olunur
- changePassword_wrongCurrentPassword_throws
- requestReset_existingUser_logsToken
- requestReset_nonExistingUser_noError (security: user yoxluğunu açıqlamır)
- confirmReset_success
- confirmReset_expiredToken_throws

Hər test üçün @ExtendWith(MockitoExtension.class) istifadə et.
Real DB lazım deyil — bütün repo-lar mock.
```

---

### PROMPT G-2: Integration Tests — IAM + Policy + Claim (Testcontainers)
- `[ ]` **G-2**

```
SaglamOL-da Testcontainers ilə integration testlər yaz.

IAM Integration Test (iam-service):
- @SpringBootTest + @Testcontainers
- PostgreSQL container
- testRegisterAndLogin_happyPath:
  1. POST /api/v1/iam/register → 201
  2. POST /api/v1/iam/login → access token + refresh token
  3. GET /api/v1/iam/me → user məlumatları
  4. POST /api/v1/iam/refresh → yeni token
  5. POST /api/v1/iam/logout → 200

Policy Integration Test (policy-service):
- @SpringBootTest + @Testcontainers
- PostgreSQL + Redis container
- testIssuePolicyAndCheckEligibility:
  1. InsurancePlan mövcuddur (seed data)
  2. AGENT rolu ilə policy yarat
  3. Eligibility check → eligible
  4. Limit rezerv et
  5. Rezervi confirm et
  6. Eligibility check → reservedAmount güncəllənib

Claim Integration Test (claim-service):
- @SpringBootTest + @Testcontainers
- PostgreSQL + Kafka (embedded kafka ya da KafkaContainer)
- testSubmitClaim_publishesOutboxEvent:
  1. Claim submit et (policy-service mock)
  2. OutboxEvent DB-ə yazıldığını yoxla
  3. OutboxWorker-i manual trigger et
  4. Kafka consumer event aldığını yoxla (EmbeddedKafka)

build.gradle-a testImplementation 'org.testcontainers:testcontainers',
'org.testcontainers:postgresql', 'org.testcontainers:kafka' əlavə et.
```

---

### PROMPT G-3: OpenAPI + Postman Collection
- `[ ]` **G-3**

```
SaglamOL layihəsinin bütün servislərinə OpenAPI (Swagger) sənədləşməsi əlavə et.

1. springdoc-openapi-starter-webmvc-ui:2.6.0 dependency-ni 
   bütün servislərin build.gradle-ına əlavə et (policy-service, claim-service, 
   user-profile-service, health-record-service, ai-risk-service, 
   fraud-detection-service, notification-service, payment-service).

2. Hər servis üçün OpenApiConfig sinfi yarat:
   @OpenAPIDefinition ilə:
   - title: "SaglamOL [Servis Adı] API"
   - version: "1.0"  
   - description: Servisin qısa izahı
   - SecurityScheme: BearerAuth (JWT)

3. application.yml-ə hər servis üçün əlavə et:
   springdoc.swagger-ui.path: /swagger-ui.html
   springdoc.api-docs.path: /api-docs

4. Bütün Controller method-larında mövcud olmayan @Operation, @ApiResponse 
   annotation-ları əlavə et.

5. docs/ qovluğunda postman_collection.json faylı yarat:
   IAM service üçün Postman Collection (JSON v2.1 format):
   - Register
   - Login (email)
   - Login (phone)
   - Refresh Token
   - Get Me
   - Change Password
   - Policy: Get Plans, Issue Policy, Get My Policies
   - Claim: Submit Claim, Get My Claims, Review Claim (approve/reject)
   
   Collection-da environment variables: {{base_url}}, {{access_token}}, {{refresh_token}}
   Pre-request script: login response-dan token-ı avtomatik set et.
```

---

### PROMPT G-4: Demo Seed Data + Final Docker Compose yoxlama
- `[ ]` **G-4**

```
SaglamOL layihəsini deploy vəziyyətinə hazırla.

1. Demo Seed Data — docker/postgres/init/ qovluğunda 01-demo-data.sql yarat:
   
   IAM demo istifadəçiləri (Liquibase-dan kənar, docker init üçün):
   Qeyd: IAM Liquibase artıq PATIENT, DOCTOR, AGENT, ADMIN rollarını yaradıb.
   Demo user-lər üçün ayrıca SQL yaz (password: Test1234! — bcrypt hash):
   - patient@saglamol.az → PATIENT rolu
   - doctor@saglamol.az → DOCTOR rolu
   - agent@saglamol.az → AGENT rolu  
   - admin@saglamol.az → ADMIN rolu
   
   Policy demo data (policy_db):
   - 3 InsurancePlan (BASIC 80 AZN/ay, STANDARD 150 AZN/ay, PREMIUM 250 AZN/ay)
   - Hər plan üçün CoverageRule (HOSPITAL 80%, DENTAL 60%, LAB 100%)

2. docker-compose.yml-i yoxla və çatışmayan hissələri tamamla:
   - Hər servisin DB-i postgres init script-dən yaradılmalıdır
   - docker/postgres/init/00-create-databases.sql yarat:
     Bütün DB-ləri yarat: iam_db, user_db, policy_db, claim_db, 
     health_record_db, ai_analysis_db, fraud_db, notification_db, payment_db
   
3. README.md-i yenilə — tam local startup rehbəri:
   - Prerequisites
   - Environment variables (.env.example-dən istifadə)
   - Addım-addım başlatma
   - Swagger URL-lər
   - Demo istifadəçilər və şifrələri
   - Postman collection istifadəsi

4. .env.example faylını yoxla — bütün vacib environment variable-lar olsun:
   POSTGRES_USER, POSTGRES_PASSWORD, IAM_JWT_SECRET, 
   MINIO_ROOT_USER, MINIO_ROOT_PASSWORD, AI_API_KEY, AI_BASE_URL, AI_MODEL
   
5. Hər servisin Dockerfile-ını yoxla — lazım gələrsə optimizasiya et 
   (multi-stage build, əgər hələ yoxdursa).
```

---

## ⚡ Tez Keçiş Cədvəli

| # | Prompt | Asılı olduğu | Est. |
|---|---|---|---|
| A-1 | Config Server | — | Sadə |
| A-2 | common-events | — | Orta |
| B-1 | user-profile: Liquibase+Repo | A-2 | Orta |
| B-2 | user-profile: Service+Controller | B-1 | Çətin |
| C-1 | policy: Liquibase+Repo | A-2 | Orta |
| C-2 | policy: Service+Controller | C-1 | Çətin |
| C-3 | policy: Internal API | C-2 | Orta |
| D-1 | claim: Entity+Liquibase | A-2, C-3 | Orta |
| D-2 | claim: Service+Outbox | D-1, C-3 | Ən çətin |
| D-3 | claim: Kafka+Feign Config | D-2 | Sadə |
| E-1 | health-record: Entity+Liquibase | A-2 | Orta |
| E-2 | health-record: MinIO+Service | E-1 | Çətin |
| F-1 | ai-risk: OpenAI+Kafka | A-2, D-2 | Çətin |
| F-2 | fraud: Rule Engine | A-2, D-2 | Çətin |
| F-3 | notification: Kafka Consumer | A-2 | Orta |
| F-4 | payment: Mock Gateway | A-2, D-2 | Orta |
| F-5 | Cross-service hash check | E-2, F-2 | Orta |
| G-1 | IAM Unit Tests | — | Orta |
| G-2 | Integration Tests | Hamısı | Çətin |
| G-3 | OpenAPI + Postman | Hamısı | Orta |
| G-4 | Seed Data + Deploy | Hamısı | Sadə |

**Toplam: 21 əsas prompt** (hər biri öz söhbətinə və ya ardıcıl)

---

## 📌 Qaydalar

> 1. **Sıranı pozma** — hər prompt öncəkinin çıxışına etibar edir
> 2. **Hər promptdan sonra** `./gradlew build` uğurlu olmalıdır
> 3. **Xəta çıxarsa** növbəti prompta keçmə — əvvəlcə həll et
> 4. **B-2 ən kritikdir** — user-profile authentication context buradan başlayır
> 5. **D-2 ən mürəkkəbdir** — bu prompta ən çox vaxt ayır

---

*Plan hazırlanma tarixi: 2026-05-24*
