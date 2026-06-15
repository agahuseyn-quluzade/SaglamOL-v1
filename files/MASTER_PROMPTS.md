# SaglamOL — Master Prompt Sənədi
## Tam İşlək Proyekt üçün Bütün Promptlar (Backend + Frontend)

> **Bu sənəd 3 mənbə faylın birləşməsidir:**
> - `files/implementation_plan.md` — Backend servisləri (21 prompt)
> - `frontend_architecture.md` — Frontend arxitektura planı
> - `deploy_ready_prompts.md` — Frontend inteqrasiya + deploy
>
> **Sıra çox vacibdir.** Hər prompt əvvəlkinin nəticəsinə əsaslanır.
> Bir prompt uğursuz olsa, növbətiyə keçmə — əvvəlcə həll et.

---

## 📋 Tamamlanma İzləyicisi

| Status | Mənası |
|--------|--------|
| `[ ]` | Gözlənilir |
| `[/]` | İcrada |
| `[x]` | Tamamdı |

---

## 🗺️ Ümumi Xəritə

```
QRUP A  — İnfrastruktur (2 prompt)                        ← Hamısından əvvəl
QRUP B  — User Profile Service (2 prompt)                 ← A sonra
QRUP C  — Policy Service (3 prompt)                       ← A sonra
QRUP D  — Claim Service (3 prompt)                        ← A, C sonra
QRUP E  — Health Record Service (2 prompt)                ← A sonra
QRUP F  — AI Risk, Fraud, Notification, Payment (5 prompt) ← A, D sonra
QRUP G  — Test + OpenAPI + Deploy (4 prompt)              ← Hamısından sonra
──────────────────────────────────────────────────────────
QRUP H  — Frontend: Auth İnteqrasiyası (3 prompt)         ← G sonra
QRUP I  — Frontend: API Client Layer (2 prompt)           ← H ilə paralel
QRUP J  — Frontend: Real Data Binding (2 prompt)          ← H, I sonra
QRUP K  — Frontend: Routing + UX Polish (4 prompt)        ← J sonra
QRUP L  — Dockerize + Deploy Hazırlığı (3 prompt)         ← K sonra
QRUP M  — End-to-End Test (2 prompt)                      ← L sonra
──────────────────────────────────────────────────────────
Toplam: 37 prompt → Tam işlək, deploy-ready proyekt
```

---

# ═══════════════════════════════════════
# BACKEND HİSSƏSİ (QRUP A–G)
# ═══════════════════════════════════════

---

## 🔵 QRUP A — İnfrastruktur

### PROMPT A-1 — Config Server konfiqurasiyası
- `[ ]` **A-1**

```
SaglamOL layihəsinin config-server-i Spring Cloud Config Server native modunda
işləyir (classpath:/config). config-server aşağıdakı servislərin konfiqurasiya
fayllarını saxlamalıdır:

infrastructure/config-server/src/main/resources/config/ qovluğunda
aşağıdakı servislərin hər biri üçün {servis-adı}.yml faylı yarat:

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

### PROMPT A-2 — common-events Kafka event sinifləri
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

Bütün event-lər Java record olsun. EventEnvelope-i generic tip parametri əlavə et:
EventEnvelope<T>.

Kafka topic sabitlərini ayrıca KafkaTopics sinifində saxla:
az.saglamol.common.events.KafkaTopics

common-events/build.gradle-a kafka dependency əlavə et.
```

---

## 🟢 QRUP B — User Profile Service

### PROMPT B-1 — User Profile Service: Liquibase, Repository, Mapper
- `[ ]` **B-1** *(A-2 tələb edir)*

```
SaglamOL layihəsinin user-profile-service modulunu tamamla.

Entity-lər artıq mövcuddur: PatientProfile, DoctorProfile, AgentProfile,
Hospital, HospitalBranch, HospitalStaffProfile, DoctorHospitalAssignment,
InsuranceCompany, InsuranceStaffProfile, Address (Embeddable).

Aşağıdakıları yarat/tamamla:

1. Liquibase:
   src/main/resources/db/changelog/db.changelog-master.xml
   src/main/resources/db/changelog/migrations/001-create-user-profile-tables.xml

   Cədvəllər: patient_profile, doctor_profile, agent_profile, hospital,
   hospital_branch, hospital_staff_profile, doctor_hospital_assignment,
   insurance_company, insurance_staff_profile

2. Repository-lər:
   - PatientProfileRepository (findByIamUserId, searchByNameOrEmail)
   - DoctorProfileRepository (findByIamUserId, findByLicenseNumber, search)
   - AgentProfileRepository (findByIamUserId, findByEmployeeCode)
   - HospitalRepository (findByRegistrationNumber)
   - HospitalBranchRepository (findAllByHospitalId)
   - HospitalStaffProfileRepository (findByIamUserIdAndHospitalId)
   - DoctorHospitalAssignmentRepository (findByDoctorProfileIdAndHospitalId)
   - InsuranceCompanyRepository
   - InsuranceStaffProfileRepository (findByIamUserIdAndCompanyId)

3. DTO-lar:
   CreatePatientProfileRequest, UpdatePatientProfileRequest, PatientProfileResponse,
   CreateDoctorProfileRequest, DoctorProfileResponse,
   CreateAgentProfileRequest, AgentProfileResponse,
   CreateHospitalRequest, HospitalResponse, CreateBranchRequest, BranchResponse,
   CreateInsuranceCompanyRequest, InsuranceCompanyResponse,
   InsuranceScopeResponse (companyId, role — internal API üçün)

4. MapStruct Mapper-lar (UserProfileMapper, HospitalMapper, InsuranceCompanyMapper)

5. application.yml-ə liquibase konfiqurasiyasını əlavə et
```

---

### PROMPT B-2 — User Profile Service: Service Layer, Controller, Security
- `[ ]` **B-2** *(B-1 tələb edir)*

```
SaglamOL user-profile-service modulunun service layer-ini tamamla.
Entity-lər, repository-lər, DTO-lar, mapper-lər artıq mövcuddur.

UserProfileService metodları:
- createPatientProfile(AuthContext, CreatePatientProfileRequest) → PatientProfileResponse
- getMyPatientProfile(AuthContext) → PatientProfileResponse
- getPatientProfileById(UUID, AuthContext) → PatientProfileResponse
- updatePatientProfile(AuthContext, UpdatePatientProfileRequest) → PatientProfileResponse
- searchPatients(query, pageable) → Page<PatientProfileResponse>  [ADMIN, AGENT]
- createDoctorProfile(AuthContext, CreateDoctorProfileRequest) → DoctorProfileResponse
- getMyDoctorProfile(AuthContext) → DoctorProfileResponse
- getDoctorById(UUID) → DoctorProfileResponse
- searchDoctors(query, pageable) → Page<DoctorProfileResponse>
- createAgentProfile(AuthContext, CreateAgentProfileRequest) → AgentProfileResponse
- getMyAgentProfile(AuthContext) → AgentProfileResponse
- getAgentsByCompany(UUID companyId) → List<AgentProfileResponse>
- assignAgentToCompany(agentProfileId, companyId) → void  [ADMIN]

HospitalService metodları:
- createHospital(AuthContext, CreateHospitalRequest) → HospitalResponse  [ADMIN]
- getHospital(hospitalId) → HospitalResponse
- getAllHospitals(pageable) → Page<HospitalResponse>
- updateHospital(hospitalId, data) → HospitalResponse  [ADMIN, HOSPITAL_ADMIN]
- updateHospitalStatus(hospitalId, status) → void  [ADMIN]
- createBranch(hospitalId, CreateBranchRequest) → BranchResponse  [ADMIN, HOSPITAL_ADMIN]
- getBranches(hospitalId) → List<BranchResponse>
- getHospitalDoctors(hospitalId) → List<DoctorProfileResponse>
- addDoctorToHospital(hospitalId, doctorProfileId) → void  [ADMIN, HOSPITAL_ADMIN]
- getHospitalStaff(hospitalId) → List<HospitalStaffProfileResponse>
- addHospitalStaff(hospitalId, data) → HospitalStaffProfileResponse  [ADMIN, HOSPITAL_ADMIN]

InsuranceCompanyService metodları:
- createCompany(data) → InsuranceCompanyResponse  [ADMIN]
- getAllCompanies(pageable) → Page<InsuranceCompanyResponse>
- getCompanyById(companyId) → InsuranceCompanyResponse
- updateCompany(companyId, data) → InsuranceCompanyResponse  [ADMIN, INSURANCE_ADMIN]
- updateCompanyStatus(companyId, status) → void  [ADMIN]
- getCompanyStaff(companyId) → List<InsuranceStaffProfileResponse>
- addCompanyStaff(companyId, data) → InsuranceStaffProfileResponse  [ADMIN, INSURANCE_ADMIN]

InternalProfileService (servis-arası, Gateway-dən keçmir):
- getUserSummary(iamUserId) → UserSummaryResponse
- checkPatientExists(patientProfileId) → boolean
- checkDoctorExists(doctorProfileId) → boolean
- checkHospitalActiveExists(hospitalId) → boolean
- getInsuranceScope(iamUserId) → InsuranceScopeResponse (companyId + role)
- getAgentCompany(agentProfileId) → UUID companyId

Controller-lər:
- UserProfileController → /api/v1/profiles
- HospitalController → /api/v1/profiles/hospitals
- InsuranceCompanyController → /api/v1/insurance-companies
- InternalProfileController → /internal/v1/profiles (JWT-siz, daxili şəbəkə)

SecurityConfig-ə /internal/** permitAll et.
AuthContext-i common-security-dən istifadə et.
Hər endpoint üçün Swagger annotation əlavə et.
UserProfileException sinfi yarat.
```

---

## 🟡 QRUP C — Policy Service

### PROMPT C-1 — Policy Service: Liquibase, Repository, DTO, Mapper
- `[ ]` **C-1** *(A-2 tələb edir)*

```
SaglamOL layihəsinin policy-service modulunu tamamla.

Entity-lər mövcuddur: InsurancePlan, CoverageRule, Policy, PolicyLimitUsage,
PolicyLimitReservation.

Entity field-larını genişlət:

InsurancePlan: id, code, name, description, status (ACTIVE/INACTIVE),
               monthlyPrice, createdAt, updatedAt

CoverageRule: id, planId, serviceType (HOSPITAL/DENTAL/OPTICAL/MENTAL_HEALTH/PHARMACY/LAB),
              coveragePercent, annualLimit, waitingPeriodDays, createdAt

Policy: id, patientId, companyId, planId,
        status (PENDING/ACTIVE/EXPIRED/CANCELLED/SUSPENDED),
        startDate, endDate, createdAt, updatedAt

PolicyLimitUsage: id, policyId, serviceType, annualLimit, usedAmount,
                  reservedAmount, @Version

PolicyLimitReservation: id, policyId, claimId, serviceType, reservedAmount,
                        status (PENDING/CONFIRMED/RELEASED), createdAt, updatedAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-policy-tables.xml + seed:
   - 3 InsurancePlan: BASIC (80 AZN/ay), STANDARD (150 AZN/ay), PREMIUM (250 AZN/ay)
   - Hər plan üçün CoverageRule: HOSPITAL 80%, DENTAL 60%, LAB 100%

2. Repository-lər: InsurancePlanRepository, CoverageRuleRepository,
   PolicyRepository (findAllByPatientId, findAllByCompanyId, findActiveByPatientId),
   PolicyLimitUsageRepository, PolicyLimitReservationRepository

3. DTO-lar:
   CreateInsurancePlanRequest, InsurancePlanResponse,
   CreateCoverageRuleRequest, CoverageRuleResponse,
   IssuePolicyRequest(patientId, companyId, planId, startDate),
   PolicyResponse, PolicySummaryResponse,
   PolicyEligibilityResponse(eligible, reason, availableLimit),
   PolicyLimitReservationResponse

4. MapStruct Mapper-lar (PolicyMapper, InsurancePlanMapper)

5. policy-service/build.gradle-a spring-data-redis, spring-cache əlavə et

6. application.yml-ə liquibase + redis konfiqurasiyası əlavə et
```

---

### PROMPT C-2 — Policy Service: Service Layer, Controller
- `[ ]` **C-2** *(C-1 tələb edir)*

```
SaglamOL policy-service modulunun service layer-ini yarat.
Entity-lər, repository-lər, DTO-lar hazırdır.

InsurancePlanService:
- createPlan(CreateInsurancePlanRequest) → InsurancePlanResponse  [ADMIN]
- getAllPlans(status, pageable) → Page<InsurancePlanResponse>
- getPlanById(planId) → InsurancePlanResponse
- addCoverageRule(planId, CreateCoverageRuleRequest) → CoverageRuleResponse  [ADMIN]
- getCoverageRules(planId) → List<CoverageRuleResponse>
- @Cacheable(Redis, TTL 10 dəq) ilə plan + coverage rule-ları keşlə

PolicyService:
- issuePolicy(AuthContext, IssuePolicyRequest) → PolicyResponse  [AGENT, INSURANCE_ADMIN, ADMIN]
  1. patient-service-dən patientId yoxla (internal API)
  2. Plan aktiv olduğunu yoxla
  3. Policy → ACTIVE status
  4. PolicyLimitUsage hər serviceType üçün yarat
  5. Kafka: PolicyIssuedEvent
- getMyPolicies(AuthContext) → List<PolicyResponse>  [PATIENT]
- getPolicyById(policyId, AuthContext) → PolicyResponse
- searchPolicies(companyId, status, pageable) → Page<PolicySummaryResponse>
- cancelPolicy(policyId, AuthContext, reason) → PolicyResponse  [AGENT, INSURANCE_ADMIN, ADMIN]
  1. PENDING/ACTIVE → CANCELLED
  2. Aktiv limit rezervasiyaları release et
  3. Kafka: PolicyCancelledEvent
- checkEligibility(policyId, serviceType, requestedAmount) → PolicyEligibilityResponse

PolicyLimitService (claim-service tərəfindən Feign ilə çağırılır):
- reserveLimit(policyId, claimId, serviceType, amount) → PolicyLimitReservation
  (optimistic lock retry: @Retryable(maxAttempts=3))
- confirmReservation(reservationId) → void
- releaseReservation(reservationId, reason) → void

Controller-lər:
- PolicyController → /api/v1/policies
- InsuranceProductController → /api/v1/insurance-products
- CoverageRuleController → /api/v1/insurance-products/{productId}/coverage-rules
- ProviderContractController → /api/v1/provider-contracts  [stub, boş list qaytarır]
- InternalPolicyController → /internal/v1/policies (JWT-siz)

PolicyException sinfi yarat. Swagger annotation-lar əlavə et.
```

---

### PROMPT C-3 — Policy Service: Internal API (claim-service üçün)
- `[ ]` **C-3** *(C-2 tələb edir)*

```
SaglamOL policy-service moduluna daxili servis-arası API əlavə et.

PolicyInternalController (/internal/v1/policies):
- GET  /internal/v1/policies/{policyId}/eligibility?serviceType=HOSPITAL&amount=1500.00
- POST /internal/v1/policies/{policyId}/limit-reservations
       body: {claimId, serviceType, amount}
- PUT  /internal/v1/policies/limit-reservations/{reservationId}/confirm
- PUT  /internal/v1/policies/limit-reservations/{reservationId}/release
       body: {reason}
- GET  /internal/v1/policies/{policyId}/active

Bu endpoint-lər JWT-siz — SecurityConfig-ə /internal/** → permitAll (şəbəkə izolasiyası var).

PolicyInternalClient Feign interfeysi yarat.
Bu client claim-service, ai-risk-service, fraud-detection-service-ə kopyalan.
```

---

## 🟠 QRUP D — Claim Service

### PROMPT D-1 — Claim Service: Entity, Liquibase, Repository, DTO
- `[ ]` **D-1** *(A-2, C-3 tələb edir)*

```
SaglamOL claim-service modulunu tamamla.

Entity-lər mövcuddur: Claim, ClaimItem, ClaimDecision, ClaimStatusHistory,
OutboxEvent, ConsumerProcessedEvent.

Entity field-larını genişlət:

Claim: id, patientId, policyId, hospitalId (nullable), companyId,
       status (DRAFT/SUBMITTED/IN_REVIEW/APPROVED/REJECTED/
               NEEDS_MORE_DOCUMENTS/PAYMENT_PENDING/PAID/PAYOUT_FAILED/CANCELLED),
       claimType (HOSPITAL/DENTAL/OPTICAL/MENTAL_HEALTH/PHARMACY/LAB),
       totalAmount, submittedAt, updatedAt, createdAt,
       riskScore (BigDecimal nullable), riskLevel (String nullable),
       fraudScore (BigDecimal nullable), fraudPassed (Boolean nullable),
       limitReservationId (UUID nullable)

ClaimItem: id, claimId, code, serviceType, description, quantity,
           unitPrice, amount, serviceDate, documentId (UUID nullable)

ClaimDecision: id, claimId, decision (APPROVED/REJECTED/MORE_DOCUMENTS),
               decidedBy (UUID), reason, note, approvedAmount (nullable), createdAt

ClaimStatusHistory: id, claimId, oldStatus, newStatus, changedAt,
                    changedBy (UUID nullable), note

OutboxEvent: id, aggregateType, aggregateId, eventType, payload (JSON),
             status (PENDING/PUBLISHED/FAILED), retryCount, nextRetryAt,
             publishedAt, lastError, createdAt

ConsumerProcessedEvent: id, topic, partitionId, offsetValue, processedAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-claim-tables.xml

2. Repository-lər:
   ClaimRepository (findAllByPatientId, findAllByCompanyId, findAllByHospitalId,
                    findByIdAndPatientId, searchWithFilters),
   ClaimItemRepository, ClaimDecisionRepository, ClaimStatusHistoryRepository,
   OutboxEventRepository (findTop50ByStatusAndNextRetryAtBefore),
   ConsumerProcessedEventRepository (existsByTopicAndPartitionIdAndOffsetValue)

3. DTO-lar:
   CreateClaimRequest(policyId, hospitalId, patientId),
   ClaimItemRequest(code, description, quantity, unitPrice),
   ClaimItemResponse, AttachClaimDocumentRequest(documentId, type),
   ClaimResponse, ClaimSummaryResponse,
   ReviewClaimRequest(note, reason),
   ClaimSearchFilters(companyId, hospitalId, patientId, status, q, page, size)

4. MapStruct Mapper (ClaimMapper)

5. claim-service/build.gradle-a:
   spring-cloud-starter-openfeign, spring-kafka əlavə et
```

---

### PROMPT D-2 — Claim Service: Service Layer, State Machine, OutboxWorker
- `[ ]` **D-2** *(D-1, C-3 tələb edir)*

```
SaglamOL claim-service modulunun service layer-ini yarat.
Entity-lər, repository-lər, DTO-lar hazırdır.

ClaimService:
- createDraft(AuthContext, CreateClaimRequest) → ClaimResponse  [PATIENT, HOSPITAL_STAFF]
- addItem(claimId, AuthContext, ClaimItemRequest) → ClaimItemResponse
- attachDocument(claimId, AuthContext, AttachClaimDocumentRequest) → void
- submitClaim(claimId, AuthContext) → ClaimResponse
  Axın:
  1. DRAFT → SUBMITTED (state machine yoxla)
  2. policy-service eligibility yoxla (PolicyInternalClient)
  3. Limit rezerv et
  4. Claim status → SUBMITTED, limitReservationId saxla
  5. ClaimStatusHistory yaz
  6. OutboxEvent yaz (ClaimSubmittedEvent) — eyni @Transactional
- getMyClaims(AuthContext, page, size) → Page<ClaimSummaryResponse>  [PATIENT]
- getClaimById(claimId, AuthContext) → ClaimResponse
- searchClaims(ClaimSearchFilters) → Page<ClaimSummaryResponse>  [AGENT, INSURANCE_STAFF, ADMIN]

ClaimReviewService:
- startReview(claimId, AuthContext) → ClaimResponse  [AGENT, INSURANCE_STAFF, ADMIN]
  SUBMITTED → IN_REVIEW
- approveClaim(claimId, AuthContext, ReviewClaimRequest) → ClaimResponse
  IN_REVIEW → APPROVED; limit confirm; OutboxEvent: ClaimApprovedEvent + ClaimPayoutRequestedEvent
- rejectClaim(claimId, AuthContext, ReviewClaimRequest) → ClaimResponse
  IN_REVIEW → REJECTED; limit release; OutboxEvent: ClaimRejectedEvent
- requestMoreDocuments(claimId, AuthContext, reason) → ClaimResponse
  IN_REVIEW → NEEDS_MORE_DOCUMENTS
- resubmitClaim(claimId, AuthContext) → ClaimResponse  [PATIENT]
  NEEDS_MORE_DOCUMENTS → SUBMITTED
- cancelClaim(claimId, AuthContext, reason) → ClaimResponse
  DRAFT/SUBMITTED → CANCELLED; limit release
- retryPayout(claimId, AuthContext) → ClaimResponse  [AGENT, ADMIN]
  PAYOUT_FAILED → yenidən ClaimPayoutRequestedEvent publish et

Kafka Consumers (idempotency: ConsumerProcessedEvent ilə):
- RiskAnalysisConsumer (risk.analysis.completed) → Claim.riskScore, riskLevel yenilə
- FraudCheckConsumer (fraud.check.completed) → Claim.fraudScore, fraudPassed yenilə
- PaymentConsumer (payment.payout.completed) → PAID; (payment.payout.failed) → PAYOUT_FAILED

OutboxWorker (@Scheduled hər 5 saniyə):
1. PENDING outbox event-ləri oxu (limit 50)
2. Kafka-ya publish et (EventEnvelope<T>)
3. status=PUBLISHED, publishedAt=now()
4. Xəta: retryCount++, nextRetryAt=now+backoff, lastError

ClaimController → /api/v1/claims
ClaimReviewController → /api/v1/claims/{claimId}/review
ClaimInternalController → /internal/v1/claims (JWT-siz)
ClaimException sinfi yarat. Swagger annotation-lar.
```

---

### PROMPT D-3 — Claim Service: Kafka + Feign Config
- `[ ]` **D-3** *(D-2 tələb edir)*

```
SaglamOL claim-service moduluna Kafka producer/consumer və Feign client
konfiqurasiyasını əlavə et.

1. KafkaProducerConfig:
   - key: String, value: JsonSerializer
   - Topic-lər: KafkaTopics sabitlərindən

2. KafkaConsumerConfig:
   - Group id: claim-service-group
   - Topics: risk.analysis.completed, fraud.check.completed,
             payment.payout.completed, payment.payout.failed
   - Deserializer: JsonDeserializer
   - Manual acknowledge
   - concurrency: 3

3. FeignConfig:
   - Eureka discovery ilə
   - Timeout: connect=2s, read=5s
   - PolicyInternalClient → policy-service
   - Header interceptor: INTERNAL_SERVICE_SECRET header

4. application.yml-ə:
   kafka bootstrap-servers, consumer/producer config,
   feign config, scheduling.enabled: true, @EnableScheduling
```

---

## 🔵 QRUP E — Health Record Service

### PROMPT E-1 — Health Record Service: Entity, Liquibase, Repository, DTO
- `[ ]` **E-1** *(A-2 tələb edir)*

```
SaglamOL health-record-service modulunu tamamla.

Entity-lər mövcuddur: HealthRecord, Treatment, MedicalDocument,
DocumentHashIndex, HealthAccessLog.

Field-ları genişlət:

HealthRecord: id, patientId, doctorId (nullable), hospitalId (nullable),
              visitDate, visitType (INPATIENT/OUTPATIENT/EMERGENCY),
              diagnosis, notes, status (ACTIVE/ARCHIVED), createdAt, updatedAt

Treatment: id, healthRecordId, treatmentType, description,
           startDate, endDate, medications, createdAt

MedicalDocument: id, healthRecordId, patientId,
                 documentType (LAB_RESULT/PRESCRIPTION/IMAGING/DISCHARGE_SUMMARY/OTHER),
                 fileName, fileSize, contentType, minioKey,
                 status (PENDING_UPLOAD/CONFIRMED/UPLOAD_FAILED),
                 sha256Hash (nullable), createdAt, updatedAt

DocumentHashIndex: id, sha256Hash, documentId, patientId, createdAt

HealthAccessLog: id, healthRecordId, accessedByUserId, accessRole, accessedAt, reason

Aşağıdakıları yarat:
1. Liquibase: 001-create-health-record-tables.xml

2. Repository-lər:
   HealthRecordRepository (findAllByPatientId, findAllByHospitalId, findAllByDoctorId),
   TreatmentRepository,
   MedicalDocumentRepository (findByMinioKey, findBySha256Hash),
   DocumentHashIndexRepository (existsBySha256HashAndPatientIdNot),
   HealthAccessLogRepository

3. DTO-lar:
   CreateHealthRecordRequest, HealthRecordResponse,
   CreateTreatmentRequest, TreatmentResponse,
   InitiateDocumentUploadRequest(fileName, fileSize, contentType, documentType),
   InitiateDocumentUploadResponse(documentId, presignedUploadUrl, expiresAt),
   ConfirmDocumentUploadRequest(sha256Hash),
   MedicalDocumentResponse,
   DocumentHashResponse (internal: sha256Hash + patientId)

4. MapStruct Mapper (HealthRecordMapper, MedicalDocumentMapper)

5. health-record-service/build.gradle-a:
   spring-kafka, io.minio:minio:8.5.7 əlavə et

6. application.yml-ə MinIO konfiqurasiyası əlavə et
```

---

### PROMPT E-2 — Health Record Service: MinIO Service Layer, Controller
- `[ ]` **E-2** *(E-1 tələb edir)*

```
SaglamOL health-record-service modulunun service layer-ini yarat.

MinioStorageService:
- generatePresignedUploadUrl(objectKey, expiryMinutes) → String
- generatePresignedDownloadUrl(objectKey, expiryMinutes) → String
- deleteObject(objectKey) → void
- ensureBucketExists() → void  (bucket: medical-documents)

HealthRecordService:
- createHealthRecord(AuthContext, CreateHealthRecordRequest) → HealthRecordResponse  [DOCTOR, HOSPITAL_STAFF]
- getMyHealthRecords(AuthContext, pageable) → Page<HealthRecordResponse>  [PATIENT]
- getHealthRecord(healthRecordId, AuthContext) → HealthRecordResponse  (HealthAccessLog yaz)
- getHospitalRecords(hospitalId, AuthContext, pageable) → Page<HealthRecordResponse>  [DOCTOR, HOSPITAL_STAFF, HOSPITAL_ADMIN]
- addTreatment(healthRecordId, AuthContext, CreateTreatmentRequest) → TreatmentResponse  [DOCTOR]

MedicalDocumentService:
- initiateUpload(AuthContext, healthRecordId, InitiateDocumentUploadRequest)
    → InitiateDocumentUploadResponse
  1. MedicalDocument yaz (status=PENDING_UPLOAD)
  2. MinIO presigned PUT URL yarat (15 dəq)
  3. URL-i response-da qaytar
- confirmUpload(AuthContext, documentId, ConfirmDocumentUploadRequest) → MedicalDocumentResponse
  1. sha256Hash yoxla — DocumentHashIndex-ə bax
  2. status=CONFIRMED yenilə
  3. DocumentHashIndex yaz
  4. Kafka: MedicalDocumentUploadedEvent
- getDocument(documentId, AuthContext) → presigned download URL (5 dəq) + metadata

Controller-lər:
- HealthRecordController → /api/v1/health-records
- MedicalDocumentController → /api/v1/health-records/{healthRecordId}/documents
- DocumentInternalController → /internal/v1/documents
  GET /internal/v1/documents/{documentId}/hash → DocumentHashResponse

HealthRecordException sinfi yarat. Kafka producer config. Swagger annotation-lar.
```

---

## 🔴 QRUP F — AI Risk, Fraud, Notification, Payment

### PROMPT F-1 — AI Risk Service: OpenAI İnteqrasiyası, Service Layer
- `[ ]` **F-1** *(A-2, D-2 tələb edir)*

```
SaglamOL ai-risk-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: RiskAnalysis, RiskReason, AiRequestLog.
Field-ları genişlət:

RiskAnalysis: id, claimId, patientId, policyId, score (BigDecimal 0-1),
              level (LOW/MEDIUM/HIGH/CRITICAL), confidence (BigDecimal),
              rawPrompt (Text), rawResponse (Text),
              status (PENDING/COMPLETED/FAILED),
              processingTimeMs, createdAt, completedAt

RiskReason: id, riskAnalysisId, category, description, weight

AiRequestLog: id, riskAnalysisId, model, inputTokens, outputTokens,
              costUsd (nullable), latencyMs, status (SUCCESS/FAILED/TIMEOUT), createdAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-ai-risk-tables.xml
2. Repository-lər: RiskAnalysisRepository, RiskReasonRepository, AiRequestLogRepository
3. DTO-lar: RiskAnalysisResponse, RiskReasonResponse
4. AiRiskModelClient interface: analyzeRisk(claimContext) → RiskAnalysisResult
5. OpenAiRiskModelClient implements AiRiskModelClient:
   - Spring RestClient ilə OpenAI /chat/completions-ə POST
   - Model: ${AI_MODEL:gpt-4o-mini}
   - System prompt: "You are a health insurance risk analyst. Analyze this claim
     and return JSON: {score, level, confidence, reasons}"
   - Timeout: 30 saniyə
   - Xəta: RiskAnalysis.status=FAILED
6. AiRiskService (Kafka consumer: claim.submitted, group: ai-risk-service-group):
   1. ConsumerProcessedEvent idempotency yoxla
   2. RiskAnalysis PENDING yarat
   3. OpenAI çağır (AiRequestLog yaz)
   4. RiskAnalysis COMPLETED/FAILED yenilə
   5. Kafka: RiskAnalysisCompletedEvent (topic: risk.analysis.completed)
7. KafkaConsumerConfig + KafkaProducerConfig
8. build.gradle-a spring-kafka əlavə et
9. application.yml: kafka, AI_API_KEY, AI_BASE_URL, AI_MODEL
10. AiRiskController → /ai-risk/claims/{claimId}  [AGENT, INSURANCE_STAFF, ADMIN]
```

---

### PROMPT F-2 — Fraud Detection Service: Rule Engine, Service Layer
- `[ ]` **F-2** *(A-2, D-2 tələb edir)*

```
SaglamOL fraud-detection-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: FraudCheck, FraudSignal, DocumentHashIndex.
Field-ları genişlət:

FraudCheck: id, claimId, patientId, policyId, fraudScore (BigDecimal 0-1),
            passed (boolean), status (PENDING/COMPLETED/FAILED),
            signalsCount, createdAt, completedAt

FraudSignal: id, fraudCheckId,
             signalType (DUPLICATE_DOCUMENT/FREQUENT_CLAIMS/HIGH_AMOUNT_ANOMALY/
                         SUSPICIOUS_TIMING/WAITING_PERIOD_VIOLATION),
             description, weight (BigDecimal), createdAt

DocumentHashIndex: id, sha256Hash, claimId, patientId, createdAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-fraud-detection-tables.xml
2. Repository-lər: FraudCheckRepository, FraudSignalRepository,
   DocumentHashIndexRepository (existsBySha256HashAndPatientIdNot)
3. FraudRule interface: evaluate(FraudCheckContext) → Optional<FraudSignal>
4. Fraud Rules (hər biri ayrı @Component):
   - DuplicateDocumentRule: eyni sha256Hash başqa patient-dədir?
     (health-record-service HealthRecordInternalClient Feign ilə)
   - FrequentClaimsRule: son 30 gündə eyni patientId-dən 3+ claim?
   - HighAmountAnomalyRule: son 6 ayın ortalamasının 3x-dən çoxdursa?
   - WaitingPeriodRule: coverageRule.waitingPeriodDays keçibmi? (PolicyInternalClient)
   - SuspiciousTimingRule: policy yaranma − claim tarixi < 7 gün?
5. FraudDetectionService (Kafka consumer: claim.submitted, group: fraud-service-group):
   1. ConsumerProcessedEvent idempotency yoxla
   2. FraudCheck PENDING yarat
   3. Bütün rule-ları paralel işlət
   4. Score = min(1.0, Σ(signal.weight)); passed = (score < 0.6)
   5. FraudCheck COMPLETED yenilə
   6. Document hash-ləri öz DocumentHashIndex-inə yaz
   7. Kafka: FraudCheckCompletedEvent (topic: fraud.check.completed)
6. KafkaConsumerConfig + KafkaProducerConfig
7. build.gradle-a spring-kafka, openfeign əlavə et
8. FraudController → /fraud/claims/{claimId}, /fraud/companies/{id}/summary,
   /fraud/hospitals/{id}/summary  [AGENT, INSURANCE_STAFF, ADMIN]
   (QEYD: prefix /fraud — /api/v1/ yoxdur)
```

---

### PROMPT F-3 — Notification Service: Template, Kafka Consumer, Mock Sender
- `[ ]` **F-3** *(A-2 tələb edir)*

```
SaglamOL notification-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: Notification, NotificationTemplate, NotificationRetry.
Field-ları genişlət:

NotificationTemplate: id, code (CLAIM_SUBMITTED/CLAIM_APPROVED/CLAIM_REJECTED/
                      CLAIM_MORE_DOCUMENTS/POLICY_ISSUED/POLICY_CANCELLED/
                      PASSWORD_RESET/WELCOME), channel (EMAIL/SMS/IN_APP),
                      subject (nullable), bodyTemplate (Mustache format), active, createdAt

Notification: id, recipientUserId, channel, templateCode, subject, body,
              status (PENDING/SENT/FAILED/PERMANENTLY_FAILED),
              retryCount, maxRetries(=3), sentAt, createdAt, updatedAt

NotificationRetry: id, notificationId, attemptNumber, failureReason, attemptedAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-notification-tables.xml + 002-seed-templates.xml
   Seed: bütün template kodları üçün Azərbaycan dilindəi şablonlar
2. Repository-lər: NotificationRepository, NotificationTemplateRepository,
   NotificationRetryRepository
3. NotificationSender interface: send(Notification) → boolean
4. MockEmailSender: LOGGER.info("📧 EMAIL → userId={}", ...) — həmişə true
5. MockSmsSender: LOGGER.info("📱 SMS → userId={}", ...) — həmişə true
6. InAppNotificationService: DB-ə yaz (status=SENT)
7. TemplateRenderer: Mustache ilə (bodyTemplate + variables)
8. NotificationService:
   - sendNotification(recipientUserId, templateCode, variables, channel)
   - getMyNotifications(userId, pageable) → Page<NotificationResponse>
   - markAsRead(notificationId, userId) → void
9. Kafka Consumers (group: notification-service-group):
   claim.approved, claim.rejected, policy.issued, notification.requested
10. RetryScheduler (@Scheduled hər 1 dəq):
    FAILED, retryCount < maxRetries → yenidən cəhd; maxRetries >= → PERMANENTLY_FAILED
11. KafkaConsumerConfig, application.yml, build.gradle (spring-kafka, mustache)
12. NotificationController → /notifications/my  [cari istifadəçi]
    NotificationTemplateController → /notifications/templates  [ADMIN]
    (QEYD: prefix /notifications — /api/v1/ yoxdur)
```

---

### PROMPT F-4 — Payment Service: Transaction, Kafka, Mock Gateway
- `[ ]` **F-4** *(A-2, D-2 tələb edir)*

```
SaglamOL payment-service modulunun tam implementasiyasını yarat.

Entity-lər mövcuddur: PaymentTransaction, PaymentEventLog.
Field-ları genişlət:

PaymentTransaction: id, type (PREMIUM_PAYMENT/CLAIM_PAYOUT/REFUND),
                    relatedEntityId, companyId, patientId, amount,
                    currency(default: AZN),
                    status (PENDING/COMPLETED/FAILED/REFUNDED),
                    externalTransactionId (nullable), failureReason (nullable),
                    initiatedAt, completedAt, updatedAt

PaymentEventLog: id, transactionId, eventType, eventData (JSON), occurredAt

Aşağıdakıları yarat:
1. Liquibase: 001-create-payment-tables.xml
2. Repository-lər: PaymentTransactionRepository (findAllByPatientId, findAllByCompanyId,
   findAllByRelatedEntityId), PaymentEventLogRepository
3. DTO-lar: PaymentTransactionResponse, CreatePremiumPaymentRequest(policyId, amount),
   CreatePayoutRequest(claimId, patientId, amount, companyId)
4. PaymentGatewayClient interface:
   initiatePayment(amount, currency, metadata) → GatewayPaymentResult(externalId, success, errorMessage)
5. MockPaymentGatewayClient:
   LOGGER.info("💳 MOCK PAYMENT amount={} currency={}", ...)
   95% uğurlu (random() > 0.05), 5% failure simulyasiyası
6. PaymentService:
   - processPremiumPayment(AuthContext, CreatePremiumPaymentRequest) → PaymentTransactionResponse  [PATIENT]
   - getMyPayments(AuthContext) → List<PaymentTransactionResponse>  [PATIENT]
   - getPaymentsByPolicy(policyId) → List<PaymentTransactionResponse>
   - getPaymentsByCompany(companyId) → List<PaymentTransactionResponse>
7. Kafka Consumer (group: payment-service-group):
   Topic: claim.payout.requested:
   1. PaymentTransaction PENDING yarat
   2. MockPaymentGatewayClient çağır
   3. COMPLETED/FAILED yenilə
   4. ClaimPayoutCompletedEvent ya da ClaimPayoutFailedEvent publish et
8. KafkaConsumerConfig + KafkaProducerConfig
9. PaymentController → /api/v1/payments:
   - GET /api/v1/payments/my  [PATIENT]
   - GET /api/v1/payments/by-policy?policyId=
   - GET /api/v1/payments/by-company?companyId=
   - POST /api/v1/payments/claim-payout  [ADMIN]
10. InvoiceController → /api/v1/invoices  [stub — boş list]
```

---

### PROMPT F-5 — Fraud + Health Record: DocumentHash Cross-check İnteqrasiyası
- `[ ]` **F-5** *(E-2, F-2 tələb edir)*

```
SaglamOL-da health-record-service və fraud-detection-service arasında
dokument duplikatı yoxlaması üçün tam inteqrasiya əlavə et.

1. health-record-service DocumentInternalController-ə:
   GET /internal/v1/documents/{documentId}/hash
   → { sha256Hash: string, patientId: UUID }

2. fraud-detection-service-ə Feign client:
   HealthRecordInternalClient:
   - getDocumentHash(documentId) → DocumentHashResponse
   Timeout: connect=2s, read=3s; Header: INTERNAL_SERVICE_SECRET

3. FraudCheckContext-ə documentIds: List<UUID> əlavə et

4. DuplicateDocumentRule-u tamamla:
   - Hər documentId üçün HealthRecordInternalClient.getDocumentHash() çağır
   - DocumentHashIndex-dən eyni hash-i başqa patientId-də yoxla
   - Tapılarsa: FraudSignal(type=DUPLICATE_DOCUMENT, weight=0.8) qaytar

5. FraudDetectionService-də fraud check bitdikdən sonra:
   document hash-ləri fraud-service-nin DocumentHashIndex-inə yaz

6. docker-compose.yml-ə environment əlavə et:
   health-record-service: HEALTH_RECORD_SERVICE_URL: http://health-record-service:8085
```

---

## 🟣 QRUP G — Test + OpenAPI + Deploy

### PROMPT G-1 — IAM Service: Unit Testlər
- `[ ]` **G-1**

```
SaglamOL iam-service üçün JUnit 5 + Mockito unit testlər yaz.

IamApplicationServiceTest:
- register_success: yeni user yaradılır, PATIENT rolu alır
- register_emailAlreadyExists_throws: EMAIL_ALREADY_EXISTS
- loginWithEmail_success: aktiv user, düzgün şifrə → token qaytarır
- loginWithEmail_wrongPassword_throws: INVALID_CREDENTIALS
- loginWithEmail_inactiveUser_throws: INVALID_CREDENTIALS
- refresh_success: valid token → rotate et
- refresh_revokedToken_throws: REFRESH_TOKEN_REUSED, family revoke
- logout_success

PasswordServiceTest:
- changePassword_success: şifrə dəyişir
- changePassword_wrongCurrentPassword_throws
- requestReset_existingUser_savesToken
- requestReset_nonExistingUser_noError
- confirmReset_success
- confirmReset_expiredToken_throws

@ExtendWith(MockitoExtension.class) — real DB lazım deyil.
```

---

### PROMPT G-2 — Integration Testlər: IAM + Policy + Claim (Testcontainers)
- `[ ]` **G-2** *(Bütün servislərin service layer-i tamamdırsa)*

```
SaglamOL-da Testcontainers ilə integration testlər yaz.

IAM Integration Test:
- @SpringBootTest + @Testcontainers, PostgreSQL container
- testRegisterAndLogin_happyPath:
  1. POST /api/v1/iam/register → 201
  2. POST /api/v1/iam/login → accessToken + refreshToken
  3. GET /api/v1/iam/me → user məlumatları
  4. POST /api/v1/iam/refresh → yeni token
  5. POST /api/v1/iam/logout → 200

Policy Integration Test:
- PostgreSQL + Redis container
- testIssuePolicyAndCheckEligibility:
  1. Seed InsurancePlan mövcuddur
  2. AGENT rolu ilə policy yarat
  3. Eligibility → eligible
  4. Limit rezerv + confirm + eligibility yenidən (reservedAmount yenilənib)

Claim Integration Test:
- PostgreSQL + EmbeddedKafka
- testSubmitClaim_publishesOutboxEvent:
  1. Claim submit et (policy-service WireMock ilə mock)
  2. OutboxEvent DB-ə yazıldığını assert et
  3. OutboxWorker trigger et
  4. Kafka-ya event getdiyini assert et

build.gradle-a:
testImplementation 'org.testcontainers:testcontainers',
                   'org.testcontainers:postgresql',
                   'org.testcontainers:kafka',
                   'com.github.tomakehurst:wiremock-standalone:3.0.4'
```

---

### PROMPT G-3 — OpenAPI + Postman Collection
- `[ ]` **G-3** *(Bütün controller-lər tamamdırsa)*

```
SaglamOL layihəsinin bütün servislərinə OpenAPI sənədləşməsi əlavə et.

1. springdoc-openapi-starter-webmvc-ui:2.6.0 dependency bütün servislərin build.gradle-ına

2. Hər servis üçün OpenApiConfig sinfi:
   @OpenAPIDefinition: title, version, SecurityScheme BearerAuth (JWT)

3. application.yml-ə:
   springdoc.swagger-ui.path: /swagger-ui.html
   springdoc.api-docs.path: /api-docs

4. Controller method-larına @Operation, @ApiResponse annotation-lar

5. docs/postman_collection.json yarat (JSON v2.1):
   - IAM: Register, Login, Refresh, Get Me, Logout
   - Password: Change, Request Reset, Confirm Reset
   - Policy: Get Plans, Issue Policy, Get My Policies, Cancel Policy
   - Claim: Create Draft, Add Item, Submit, Get My Claims, Review (Approve/Reject/More Docs)
   - Health Records: Get My, Initiate Upload, Confirm Upload
   - Payments: Get My, Premium Payment
   - Notifications: Get My

   Environment: {{base_url}}=http://localhost:8080, {{access_token}}, {{refresh_token}}
   Pre-request Script (Login): response-dan token-ı environment-ə set et.
```

---

### PROMPT G-4 — Demo Seed Data + Final Docker Compose
- `[ ]` **G-4** *(Hamısı tamamdırsa)*

```
SaglamOL layihəsini tam deploy vəziyyətinə hazırla.

1. docker/postgres/init/01-demo-data.sql yarat:
   IAM demo istifadəçiləri (bcrypt hash, şifrə: Test1234!):
   - patient@saglamol.az → PATIENT
   - doctor@saglamol.az → DOCTOR
   - hospital_admin@saglamol.az → HOSPITAL_ADMIN
   - insurance_admin@saglamol.az → INSURANCE_ADMIN
   - agent@saglamol.az → AGENT
   - admin@saglamol.az → ADMIN

   Policy demo (policy_db): BASIC, STANDARD, PREMIUM planları + CoverageRule-lar

   Hospital demo (user_db): Baku Medical Center, Şəhər Klinikası

   Insurance company demo (user_db): Araz Life Insurance

2. docker-compose.yml-də bütün servislərin restart: "no" → unless-stopped

3. README.md-i yenilə: Prerequisites, .env setup, build, docker-compose up,
   Swagger URL-ləri, demo istifadəçilər

4. .env.example-da bütün production secret-lərin mütləq dəyişdirilməsini qeyd et.
```

---

# ═══════════════════════════════════════
# FRONTEND HİSSƏSİ (QRUP H–M)
# ═══════════════════════════════════════

> **Ön şərt:** QRUP A–G tamamlandıqdan sonra başla.
> Backend `docker-compose up -d` ilə ayaqda olmalıdır.
> EH-FRONT — Next.js 15, TypeScript, Zustand, TanStack Query, Axios.

---

## 🟤 QRUP H — Frontend: Auth İnteqrasiyası

### PROMPT H-1 — Login səhifəsini real backend-ə qoşmaq
- `[ ]` **H-1**

```
EH-FRONT/src/app/(public)/login/page.tsx faylında demoAccounts adlı
hardcoded mock auth var. Bunu tamamilə real backend ilə əvəzlə.

1. demoAccounts obyektini və mock login məntiğini sil.

2. Form submit handler-ini async et:
   - iamApi.login({ email, password }) çağır
   - Response: { accessToken, refreshToken, user: { email, fullName, role } }
   - setTokens(accessToken, refreshToken)
   - setUser({ email, fullName, role })
   - document.cookie = `saglamol_role=${user.role}; path=/; max-age=604800; SameSite=Lax`

   Portal routing:
   const getPortalForRole = (role: string) => ({
     PATIENT: "patient",
     DOCTOR: "hospital",
     HOSPITAL_ADMIN: "hospital",
     HOSPITAL_STAFF: "hospital",
     INSURANCE_ADMIN: "insurance",
     INSURANCE_STAFF: "insurance",
     AGENT: "insurance",
     ADMIN: "admin"
   }[role] ?? "patient")
   router.push(`/${getPortalForRole(user.role)}/dashboard`)

3. Error handling (isAxiosError ilə):
   - 401/403 → "Email və ya şifrə yanlışdır"
   - 400 → backend message
   - !error.response → "Server əlçatmazdır"
   - digər → "Gözlənilməz xəta baş verdi"

4. Loading state: useState(false), button disabled + Loader2 spinner

5. Error mesajı forumun üstündə (kırmızı border-left card)

Backend: POST /api/v1/iam/login
```

---

### PROMPT H-2 — Apply (Müraciət) səhifəsini backend-ə qoşmaq
- `[ ]` **H-2**

```
EH-FRONT/src/app/(public)/apply/page.tsx — submit handler-i yoxdur.
Bunu real backend-ə qoş.

1. React Hook Form + Zod validation:
   - applicantType: z.enum(["PATIENT", "HOSPITAL", "INSURANCE"])
   - name: z.string().min(2)
   - email: z.string().email()
   - phone: z.string().regex(/^\+994/).min(13)
   - identifier: z.string().optional()
   - password: z.string().min(8)
   - passwordConfirm uyğunluq yoxlaması

2. applicantType seçimini radio button card-lara çevir

3. Submit:
   const roleMap = { PATIENT: "PATIENT", HOSPITAL: "HOSPITAL_ADMIN", INSURANCE: "INSURANCE_ADMIN" }
   iamApi.register({ email, password, role: roleMap[applicantType] })

4. Uğurlu: "Müraciətiniz qəbul edildi" mesajı → 3 saniyə sonra /login redirect

5. Error handling: H-1 ilə eyni; 409 → "Bu email artıq qeydiyyatdan keçib"

Backend: POST /api/v1/iam/register
```

---

### PROMPT H-3 — Password Reset səhifələrini backend-ə qoşmaq
- `[ ]` **H-3**

```
EH-FRONT-dəki iki password reset səhifəsini real backend-ə qoş.

1. src/app/(public)/password-reset/request/page.tsx:
   - Zod: email validation
   - submit: passwordApi.requestReset(email)
   - Uğurlu: "Email-inizə sıfırlama linki göndərildi" success state
   - Loading state
   Backend: POST /api/v1/iam/password/reset/request

2. src/app/(public)/password-reset/confirm/page.tsx:
   - "use client" + Suspense wrapper (useSearchParams üçün)
   - URL-dən token al: useSearchParams().get("token")
   - Token yoxdursa: warning göstər, form disable
   - Zod: newPassword (min 8), confirmPassword uyğunluq
   - submit: passwordApi.confirmReset(token, newPassword)
   - Uğurlu: router.push("/login")
   - 400/404 → "Token etibarsızdır və ya vaxtı bitib"
   Backend: POST /api/v1/iam/password/reset/confirm
```

---

## 🟡 QRUP I — Frontend: API Client Layer

### PROMPT I-1 — Çatışan API Client-ləri yarat
- `[ ]` **I-1**

```
EH-FRONT/src/lib/api/ qovluğunda mövcud client-lər:
iam.api.ts, claims.api.ts, policies.api.ts, payments.api.ts,
profiles.api.ts, password.api.ts, admin.api.ts

Aşağıdakı yeni faylları yarat (mövcud client.ts import et):

1. health-records.api.ts  (Backend: /api/v1/health-records)
   export const healthRecordsApi = {
     getMyRecords: () → GET /api/v1/health-records/my
     getById: (id) → GET /api/v1/health-records/{id}
     create: (data) → POST /api/v1/health-records
     getDocuments: (recordId) → GET /api/v1/health-records/{id}/documents
     initiateUpload: (recordId, data) → POST /api/v1/health-records/{id}/documents/initiate
     confirmUpload: (documentId, data) → POST /api/v1/health-records/documents/{documentId}/confirm
   }

2. notifications.api.ts  (Backend prefix: /notifications — /api/v1/ yox)
   export const notificationsApi = {
     getMy: (page, size) → GET /notifications/my
     markAsRead: (id) → PATCH /notifications/{id}/read
     getTemplates: () → GET /notifications/templates
   }

3. fraud.api.ts  (Backend prefix: /fraud)
   export const fraudApi = {
     getCompanySummary: (companyId) → GET /fraud/companies/{companyId}/summary
     getHospitalSummary: (hospitalId) → GET /fraud/hospitals/{hospitalId}/summary
     getClaimFraudScore: (claimId) → GET /fraud/claims/{claimId}
   }

4. ai-risk.api.ts  (Backend prefix: /ai-risk)
   export const aiRiskApi = {
     analyzeClaimRisk: (claimId) → GET /ai-risk/claims/{claimId}
   }

5. insurance-products.api.ts  (Backend: /api/v1/insurance-products)
   export const insuranceProductsApi = {
     getAll: (params?) → GET /api/v1/insurance-products
     getById: (id) → GET /api/v1/insurance-products/{id}
     create: (data) → POST /api/v1/insurance-products
     update: (id, data) → PUT /api/v1/insurance-products/{id}
     getCoverageRules: (productId) → GET /api/v1/insurance-products/{productId}/coverage-rules
     addCoverageRule: (productId, data) → POST /api/v1/insurance-products/{productId}/coverage-rules
   }

6. invoices.api.ts  (Backend: /api/v1/invoices)
   export const invoicesApi = {
     getByCompany: (companyId) → GET /api/v1/invoices/by-company?companyId=
     getById: (id) → GET /api/v1/invoices/{id}
   }

7. provider-contracts.api.ts  (Backend: /api/v1/provider-contracts)
   export const providerContractsApi = {
     getByCompany: (companyId) → GET /api/v1/provider-contracts/by-company/{companyId}
     create: (data) → POST /api/v1/provider-contracts
   }

Hər faylda TypeScript DTO tipləri tanımla.
src/lib/api/index.ts barrel export faylına hamısını əlavə et.
```

---

### PROMPT I-2 — Mövcud API Client-lərə çatışan endpoint-ləri əlavə et
- `[ ]` **I-2**

```
Mövcud EH-FRONT/src/lib/api/ fayllarını genişlət:

1. profiles.api.ts-ə əlavə et:
   createPatient, getPatientById, updatePatient,
   getDoctorMe, getDoctorById, searchDoctors,
   getAgentMe, getAgentById, searchAgents,
   getHospitalDoctors(hospitalId), getHospitalStaff(hospitalId),
   getHospitalBranches(hospitalId)

2. admin.api.ts-ə əlavə et:
   createHospital, getHospitalById, updateHospital, updateHospitalStatus,
   createCompany, getCompanyById, updateCompany, updateCompanyStatus,
   getCompanyStaff, addCompanyStaff,
   addHospitalBranch, addHospitalStaff, updateHospitalStaffStatus,
   getNotificationTemplates,
   searchPatients(q), searchUsers(q)

3. payments.api.ts-ə əlavə et:
   premiumPayment(data) → POST /api/v1/payments/premium

4. Bütün yeni endpoint-lər üçün TypeScript tipləri (types.ts faylına əlavə et)
```

---

## 🟢 QRUP J — Frontend: Real Data Binding

### PROMPT J-1 — React Query Hook-ları yarat
- `[ ]` **J-1** *(I-1, I-2 tələb edir)*

```
EH-FRONT/src/lib/hooks/ qovluğunda mövcud useMockQuery.ts var.
Hər backend servisi üçün ayrıca real React Query hook faylı yarat.

1. useClaims.ts
   useMyClaimsQuery(page, size), useClaimByIdQuery(claimId),
   useClaimSearchQuery(filters), useCreateClaimMutation(),
   useSubmitClaimMutation(), useAddClaimItemMutation(),
   useClaimReviewMutation(action: "approve"|"reject"|"requestMoreDocuments"|"start"|"cancel"|"resubmit"|"retryPayout")

2. usePolicies.ts
   useMyPoliciesQuery(), usePolicyByIdQuery(id, enabled?),
   usePolicySearchQuery(params), useIssuePolicyMutation(),
   useCancelPolicyMutation(), useEligibilityCheckMutation()

3. usePayments.ts
   useMyPaymentsQuery(), usePaymentsByPolicyQuery(policyId, enabled?),
   usePaymentsByCompanyQuery(companyId, enabled?),
   useClaimPayoutMutation(), usePremiumPaymentMutation()

4. useProfiles.ts
   usePatientMeQuery(), useSearchPatientsQuery(q),
   useHospitalDoctorsQuery(hospitalId, enabled?),
   useHospitalStaffQuery(hospitalId, enabled?),
   useHospitalBranchesQuery(hospitalId, enabled?),
   useAgentByCompanyQuery(companyId, enabled?)

5. useHealthRecords.ts
   useMyHealthRecordsQuery(page), useHealthRecordByIdQuery(id),
   useInitiateUploadMutation(), useConfirmUploadMutation()

6. useNotifications.ts
   useMyNotificationsQuery(page), useMarkAsReadMutation()

7. useFraud.ts
   useFraudCompanySummaryQuery(companyId, enabled?),
   useFraudHospitalSummaryQuery(hospitalId, enabled?),
   useClaimFraudScoreQuery(claimId, enabled?)

8. useAiRisk.ts
   useClaimRiskQuery(claimId, enabled?)

9. useAdmin.ts
   useUsersQuery(), useSearchUsersQuery(q, enabled?),
   useCompaniesQuery(), useHospitalsQuery(),
   useUpdateUserStatusMutation()

10. useInsuranceProducts.ts
    useInsuranceProductsQuery(params?), useInsuranceProductByIdQuery(id),
    useCreateProductMutation(), useCoverageRulesQuery(productId),
    useAddCoverageRuleMutation()

Hər hook üçün:
- queryKey array formatı: ["claims", "my", page]
- enabled parametri ilə conditional fetch
- useMutation onSuccess → queryClient.invalidateQueries()
- Error-lar React Query-nin isError state-i ilə
```

---

### PROMPT J-2 — Domain Komponentlərini Real Data-ya Qoş, Mock-u Sil
- `[ ]` **J-2** *(J-1 tələb edir)*

```
EH-FRONT/src/components/domain/ altındakı komponentlər mock data göstərir.
Real React Query hook-larına qoş.

Pattern hər komponentdə:
  const { data, isLoading, isError } = useXxxQuery(...)
  if (isLoading) return <SkeletonCard />
  if (isError) return <ErrorCard message="Məlumat yüklənmədi" />
  if (!data) return <EmptyCard message="Məlumat tapılmadı" />

Komponentlər:
1.  domain/claim/ClaimStatusTimeline.tsx → useClaimByIdQuery()
2.  domain/claim/ClaimDocumentList.tsx → useClaimByIdQuery()
3.  domain/claim/ClaimCreationWizard.tsx →
    Step 1: useMyPoliciesQuery() — policy seçimi
    Step 2: useCreateClaimMutation() + useAddClaimItemMutation()
    Step 3: useSubmitClaimMutation() → uğurlu submit → /claims redirect
4.  domain/claim/ClaimReviewPanel.tsx → useClaimReviewMutation(action)
5.  domain/claim/ClaimRiskBanner.tsx → useClaimRiskQuery(claimId)
6.  domain/claim/ClaimFraudBanner.tsx → useClaimFraudScoreQuery(claimId)
7.  domain/policy/PolicyCard.tsx → usePolicyByIdQuery()
8.  domain/policy/PolicyLimitGauge.tsx → usePolicyByIdQuery() — limit data
9.  domain/policy/EligibilityCheckForm.tsx → useEligibilityCheckMutation()
10. domain/policy/IssuePolicyForm.tsx → useIssuePolicyMutation() + useInsuranceProductsQuery()
11. domain/payment/PaymentTimeline.tsx →
    patient: useMyPaymentsQuery()
    insurance: usePaymentsByCompanyQuery(companyId)
12. domain/payment/PayoutForm.tsx → useClaimPayoutMutation()
13. domain/payment/PremiumPaymentForm.tsx → usePremiumPaymentMutation()
14. domain/health-record/HealthRecordCard.tsx → useHealthRecordByIdQuery()
15. domain/health-record/TreatmentList.tsx → useHealthRecordByIdQuery()
16. domain/health-record/DocumentUploader.tsx →
    useInitiateUploadMutation() → presigned URL al → fetch(url, { method: 'PUT', body: file })
    → useConfirmUploadMutation()
17. domain/health-record/DocumentViewer.tsx → presigned download URL
18. domain/notification/NotificationBell.tsx → useMyNotificationsQuery(0, 5)
19. domain/notification/NotificationList.tsx → useMyNotificationsQuery(page)
20. domain/notification/NotificationItem.tsx → useMarkAsReadMutation()
21. domain/fraud/FraudSummaryCard.tsx → useFraudCompanySummaryQuery()
22. domain/fraud/FraudScoreGauge.tsx → useClaimFraudScoreQuery()
23. domain/fraud/FraudSignalList.tsx → useClaimFraudScoreQuery()
24. domain/dashboard/ClaimPipelineChart.tsx → useClaimSearchQuery()
25. domain/dashboard/RevenueChart.tsx → usePaymentsByCompanyQuery()
26. domain/dashboard/FraudHeatmap.tsx → useFraudCompanySummaryQuery()
27. domain/dashboard/RecentActivityFeed.tsx → useMyNotificationsQuery(0, 10)

Son addım:
- useMockQuery.ts faylını SİL
- lib/data/mock.ts-dəki buildPageModel() funksiyasını SİL
```

---

## 🔵 QRUP K — Frontend: Routing Refactor + UX Polish

### PROMPT K-1 — Patient Portal: Catch-All-dan Ayrı Səhifələrə
- `[ ]` **K-1** *(J-2 tələb edir)*

```
EH-FRONT-dəki /patient/[[...slug]]/page.tsx catch-all route-u silin,
aşağıdakı ayrı page.tsx fayllarını yarat:

src/app/(portal)/patient/
├── dashboard/page.tsx
│   StatCard x4 (claim/policy sayları)
│   ClaimPipelineChart + RecentActivityFeed
├── profile/page.tsx  (usePatientMeQuery + update form)
├── policies/
│   ├── page.tsx  (useMyPoliciesQuery → PolicyCard list + EmptyState)
│   └── [policyId]/page.tsx  (PolicyCard + PolicyLimitGauge + PaymentTimeline)
├── claims/
│   ├── page.tsx  (useMyClaimsQuery → DataTable + Pagination)
│   ├── new/page.tsx  (ClaimCreationWizard)
│   └── [claimId]/page.tsx  (ClaimStatusTimeline + ClaimDocumentList + Risk/Fraud banners)
├── health-records/
│   ├── page.tsx  (useMyHealthRecordsQuery → HealthRecordCard list)
│   └── [recordId]/page.tsx  (TreatmentList + DocumentViewer + DocumentUploader)
├── payments/page.tsx  (useMyPaymentsQuery + PremiumPaymentForm)
├── notifications/page.tsx  (useMyNotificationsQuery → NotificationList)
└── settings/page.tsx  (şifrə dəyişmə formu, Zod validation)

Hər page.tsx üçün:
- "use client" directive
- generateMetadata export et
- Uyğun loading.tsx faylı
patient/[[...slug]] qovluğunu SİL.
```

---

### PROMPT K-2 — Hospital və Admin Portal: Ayrı Səhifələr
- `[ ]` **K-2** *(K-1 sonra)*

```
Hospital portalu:
src/app/(portal)/hospital/
├── dashboard/page.tsx  (useClaimSearchQuery + useFraudHospitalSummaryQuery)
├── claims/
│   ├── page.tsx, new/page.tsx, [claimId]/page.tsx
├── health-records/page.tsx, [recordId]/page.tsx
├── documents/upload/page.tsx  (DocumentUploader)
├── doctors/page.tsx  (useHospitalDoctorsQuery → DataTable)
├── staff/page.tsx  (useHospitalStaffQuery → DataTable)
└── branches/page.tsx  (useHospitalBranchesQuery → DataTable)

hospitalId → auth store + useHospitalMeQuery (hook əlavə et)

Admin portalu:
src/app/(portal)/admin/
├── dashboard/page.tsx  (system-wide stats)
├── users/page.tsx, [userId]/page.tsx
├── companies/page.tsx, [companyId]/page.tsx
├── hospitals/page.tsx, [hospitalId]/page.tsx
├── patients/page.tsx  (search)
└── notifications/templates/page.tsx

hospital/[[...slug]] və admin/[[...slug]] qovluqlarını SİL.
```

---

### PROMPT K-3 — Insurance Portal: Ayrı Səhifələr
- `[ ]` **K-3** *(K-1 sonra)*

```
Insurance portalu:
src/app/(portal)/insurance/
├── dashboard/page.tsx  (useClaimSearchQuery + useFraudCompanySummaryQuery + charts)
├── claims/
│   ├── page.tsx  (filter + DataTable + Pagination)
│   └── [claimId]/
│       ├── page.tsx  (ClaimStatusTimeline + ClaimDocumentList)
│       └── review/page.tsx  (ClaimReviewPanel + Risk/Fraud banners)
├── policies/
│   ├── page.tsx, issue/page.tsx, [policyId]/page.tsx
├── products/page.tsx, [productId]/page.tsx
├── payments/page.tsx, payout/new/page.tsx
├── invoices/page.tsx  (invoicesApi.getByCompany → DataTable)
├── fraud/page.tsx  (FraudSummaryCard + FraudScoreGauge + FraudSignalList)
├── risk/demo-claim/page.tsx
├── contracts/page.tsx  (providerContractsApi → DataTable)
├── staff/page.tsx  (getCompanyStaff → DataTable)
├── agents/page.tsx  (useAgentByCompanyQuery → DataTable)
└── company/page.tsx  (getCompanyById → edit form)

companyId → auth store + profilesApi-dən al
insurance/[[...slug]] qovluğunu SİL.
```

---

### PROMPT K-4 — Global Error Handling, Toast, Skeleton, Responsive
- `[ ]` **K-4** *(K-1, K-2, K-3 sonra)*

```
EH-FRONT-ə UX polish əlavə et:

1. Toast sistemi (Zustand):
   src/lib/stores/toast.store.ts:
   type Toast = { id: string; message: string; type: "success"|"error"|"warning"|"info" }
   addToast(message, type), removeToast(id)

   src/components/ui/Toast.tsx:
   - Fixed position sağ üst, z-index: 9999
   - Rəng kodlaması: success=yaşıl, error=qırmızı, warning=sarı, info=mavi border-left
   - Auto-dismiss: 5 saniyə (setTimeout)
   - Manual close (X button)
   - Slide-in animation: @keyframes slideIn

2. Axios interceptor (client.ts-ə əlavə et):
   - 403 → toast("Bu əməliyyata icazəniz yoxdur", "error")
   - 404 → toast("Məlumat tapılmadı", "error")
   - 409 → toast(backendMessage || "Məlumat konflikti", "error")
   - 500 → toast("Server xətası, yenidən cəhd edin", "error")
   - network → toast("Server əlçatmazdır", "error")
   (401 ARTIQ auto-refresh edir — toast əlavə etmə)

3. src/components/ui/Skeleton.tsx:
   SkeletonLine, SkeletonCard, SkeletonTable({ rows = 5 })
   CSS: @keyframes pulse { 0%, 100% { opacity: 1 } 50% { opacity: 0.4 } }

4. Hər portal route üçün loading.tsx (SkeletonTable ya da SkeletonCard istifadə et)

5. DataTable-a pagination əlavə et:
   Props: totalPages, currentPage, onPageChange
   Prev/Next button + "Səhifə X / Y"

6. Responsive Sidebar (PortalShell.tsx):
   - Desktop (>1024px): 260px, həmişə açıq
   - Tablet (768-1024px): 72px (yalnız ikonlar)
   - Mobile (<768px): gizli, hamburger menü ilə açılır

7. Dark mode persist (ui.store.ts):
   Zustand persist middleware, localStorage key: "saglamol-ui"
```

---

## 🟣 QRUP L — Dockerize + Deploy Hazırlığı

### PROMPT L-1 — Frontend Dockerfile + Health Check Route
- `[ ]` **L-1** *(K-4 sonra)*

```
EH-FRONT layihəsini Docker ilə işlək et.

1. EH-FRONT/next.config.ts-ə: output: "standalone"

2. EH-FRONT/src/app/api/health/route.ts:
   import { NextResponse } from "next/server";
   export function GET() {
     return NextResponse.json({ status: "ok", timestamp: Date.now() });
   }

3. EH-FRONT/Dockerfile (multi-stage):

   # Stage 1: deps
   FROM node:20-alpine AS deps
   WORKDIR /app
   COPY package.json package-lock.json ./
   RUN npm ci --omit=dev

   # Stage 2: builder
   FROM node:20-alpine AS builder
   WORKDIR /app
   COPY --from=deps /app/node_modules ./node_modules
   COPY . .
   ENV NEXT_TELEMETRY_DISABLED=1
   ARG NEXT_PUBLIC_API_URL=http://localhost:8080
   ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
   RUN npm run build

   # Stage 3: runner
   FROM node:20-alpine AS runner
   WORKDIR /app
   ENV NODE_ENV=production
   ENV NEXT_TELEMETRY_DISABLED=1
   RUN addgroup -S app && adduser -S app -G app
   COPY --from=builder /app/.next/standalone ./
   COPY --from=builder /app/.next/static ./.next/static
   COPY --from=builder /app/public ./public
   USER app
   EXPOSE 3000
   HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
     CMD wget -qO- http://localhost:3000/api/health || exit 1
   CMD ["node", "server.js"]

4. EH-FRONT/.dockerignore:
   node_modules
   .next
   .env.local
   .git
   *.md
```

---

### PROMPT L-2 — Docker Compose-a Frontend Əlavə et
- `[ ]` **L-2** *(L-1 sonra)*

```
SaglamOL/docker-compose.yml services bölməsinə frontend əlavə et:

  frontend:
    build:
      context: ../EH-FRONT
      dockerfile: Dockerfile
      args:
        NEXT_PUBLIC_API_URL: http://localhost:8080
    image: saglamol/frontend:local
    container_name: saglamol-frontend
    ports:
      - "3001:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://api-gateway:8080
    depends_on:
      api-gateway:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:3000/api/health || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped
    networks:
      - saglamol-network

QEYD: NEXT_PUBLIC_API_URL build-time ARG ilə localhost:8080 olmalıdır
(browser sorğuları üçün). Container içindəki SSR sorğuları api-gateway adını istifadə edir.
```

---

### PROMPT L-3 — API Gateway CORS + Production .env
- `[ ]` **L-3**

```
1. API Gateway CORS konfiqurasiyası:
   spring.cloud.gateway.globalcors.cors-configurations:
     '[/**]':
       allowed-origins:
         - "http://localhost:3000"
         - "http://localhost:3001"
       allowed-methods: ["GET","POST","PUT","PATCH","DELETE","OPTIONS"]
       allowed-headers: ["*"]
       exposed-headers: ["Authorization"]
       allow-credentials: true
       max-age: 3600

   Əgər Spring Security var, CorsConfigurationSource bean:
   source.registerCorsConfiguration("/**", config)
   http.cors(cors -> cors.configurationSource(source))

2. SaglamOL/.env.production.example yarat:
   POSTGRES_PASSWORD=<min-32-char>
   IAM_JWT_SECRET=<min-64-char>
   INTERNAL_SERVICE_SECRET=<min-48-char>
   MINIO_ROOT_PASSWORD=<min-32-char>
   AI_API_KEY=<real-key>
   AI_BASE_URL=https://api.openai.com/v1
   AI_MODEL=gpt-4o-mini
   GRAFANA_ADMIN_PASSWORD=<güclü-şifrə>

3. .gitignore-a: .env.production əlavə et

4. README.md-ə production bölməsi:
   - .env.production.example-dən .env.production yarat
   - docker-compose --env-file .env.production up -d
   - Bütün secret-lərin dəyişdirilməsi məcburi
```

---

## 🔵 QRUP M — End-to-End Test

### PROMPT M-1 — Backend Smoke Test
- `[ ]` **M-1** *(L-2, L-3 sonra)*

```
Backend servislərin tam işlək olduğunu yoxla.

1. Build + Deploy:
   cd SaglamOL
   ./gradlew clean build -x test
   docker-compose build
   docker-compose up -d

2. Sağlamlıq yoxlaması (hər servis üçün):
   curl http://localhost:8761/actuator/health  (discovery-server)
   curl http://localhost:8888/actuator/health  (config-server)
   curl http://localhost:8080/actuator/health  (api-gateway)
   curl http://localhost:8081/actuator/health  (iam-service)
   curl http://localhost:8082/actuator/health  (user-profile-service)
   curl http://localhost:8083/actuator/health  (policy-service)
   curl http://localhost:8084/actuator/health  (claim-service)
   curl http://localhost:8085/actuator/health  (health-record-service)
   curl http://localhost:8086/actuator/health  (ai-risk-service)
   curl http://localhost:8087/actuator/health  (fraud-detection-service)
   curl http://localhost:8088/actuator/health  (notification-service)
   curl http://localhost:8089/actuator/health  (payment-service)

3. Demo login:
   curl -X POST http://localhost:8080/api/v1/iam/login \
     -H "Content-Type: application/json" \
     -d '{"email":"patient@saglamol.az","password":"Test1234!"}'
   → accessToken + refreshToken alınmalıdır

4. Protected endpoint:
   curl -H "Authorization: Bearer <accessToken>" \
     http://localhost:8080/api/v1/policies/me

5. MinIO: http://localhost:9001 → login ✅
6. Kafka topics siyahısı görünürmü? ✅
7. Grafana: http://localhost:3000 ✅

Xəta varsa: docker logs <container-name> --tail 100
```

---

### PROMPT M-2 — Frontend End-to-End İnteqrasiya Test
- `[ ]` **M-2** *(M-1 sonra)*

```
Frontend + Backend birlikdə işlədiyini test et.

1. Dev server:
   cd EH-FRONT && npm run dev
   Browser: http://localhost:3000

2. Auth flow:
   a. /apply → müraciət formu doldur → uğurlu mesaj?
   b. /login → patient@saglamol.az / Test1234! → /patient/dashboard?
   c. DevTools Network → /api/v1/iam/login → 200 OK?
   d. Logout → /login?

3. Patient portal:
   /patient/policies → real policy data? (seed datadan BASIC/STANDARD/PREMIUM)
   /patient/claims → boş list (normal)
   /patient/claims/new → ClaimCreationWizard açılır?
   /patient/health-records → boş list (normal)
   /patient/notifications → boş list (normal)

4. Insurance portal:
   insurance_admin@saglamol.az / Test1234! ilə giriş
   /insurance/claims → filter işləyir?
   /insurance/policies/issue → form görünür?
   /insurance/fraud → fraud summary gəlir?

5. Admin portal:
   admin@saglamol.az / Test1234! ilə giriş
   /admin/users, /admin/companies, /admin/hospitals → məlumat gəlir?

6. Docker tam test:
   docker-compose up -d
   Browser: http://localhost:3001
   Yuxarıdakı 2-5 testləri təkrarla

7. CORS yoxlama:
   DevTools Console-da "CORS error" yoxdursa ✅
   Varsa → L-3 CORS konfiqurasiyasını yenidən yoxla

✅ işlədisə → Proyekt tam deploy-ready!
❌ xəta varsa → error mesajını analiz et, fix et, sonra davam et.
```

---

# ═══════════════════════════════════════
# ASILILIQ CƏDVƏLİ
# ═══════════════════════════════════════

| Prompt | Tələb edir | Çətinlik |
|--------|-----------|----------|
| A-1 | — | Sadə |
| A-2 | — | Orta |
| B-1 | A-2 | Orta |
| B-2 | B-1 | Çətin |
| C-1 | A-2 | Orta |
| C-2 | C-1 | Çətin |
| C-3 | C-2 | Orta |
| D-1 | A-2, C-3 | Orta |
| D-2 | D-1, C-3 | **Ən çətin** |
| D-3 | D-2 | Sadə |
| E-1 | A-2 | Orta |
| E-2 | E-1 | Çətin |
| F-1 | A-2, D-2 | Çətin |
| F-2 | A-2, D-2 | Çətin |
| F-3 | A-2 | Orta |
| F-4 | A-2, D-2 | Orta |
| F-5 | E-2, F-2 | Orta |
| G-1 | IAM tam | Orta |
| G-2 | D-2 tam | Çətin |
| G-3 | Hamısı | Orta |
| G-4 | Hamısı | Sadə |
| H-1 | G-4 | Sadə |
| H-2 | G-4 | Sadə |
| H-3 | G-4 | Sadə |
| I-1 | H-1 | Orta |
| I-2 | I-1 | Sadə |
| J-1 | I-1, I-2 | Orta |
| J-2 | J-1 | Çətin |
| K-1 | J-2 | Çətin |
| K-2 | K-1 | Çətin |
| K-3 | K-1 | Çətin |
| K-4 | K-1,K-2,K-3 | Orta |
| L-1 | K-4 | Sadə |
| L-2 | L-1 | Sadə |
| L-3 | Backend | Orta |
| M-1 | L-2, L-3 | Orta |
| M-2 | M-1 | Orta |

**Toplam: 37 prompt → Tam işlək, deploy-ready proyekt**

---

# QAYDALAR

> 1. **Sıranı pozma** — hər prompt öncəkinin çıxışına əsaslanır
> 2. **Backend prompt-lardan sonra** `./gradlew build -x test` uğurlu olmalıdır
> 3. **Xəta çıxarsa** növbəti prompta keçmə — əvvəlcə həll et
> 4. **D-2 ən mürəkkəbdir** — ən çox vaxt bu prompta ayrılmalıdır
> 5. **J-2-dən sonra** mock data tamamilə silinir — geri qayıtmaq olmur
> 6. **Production-da** `.env.production`-dakı bütün secret-lər mütləq dəyişdirilməlidir

---

*Master sənəd hazırlanma tarixi: 2026-06-12*
*Mənbə fayllar:*
*- files/implementation_plan.md (21 backend prompt)*
*- frontend_architecture.md (arxitektura planı)*
*- deploy_ready_prompts.md (frontend inteqrasiya)*
