# SaglamOL - Fast Working Prompts

Bu fayl layiheni en qisa yolla real islek veziyyete getirmek ucun hazir
copy-paste promptlar siyahisidir. Promptlar faktiki repo veziyyetine esaslanir:
backend `SaglamOL`, frontend `EH-FRONT`.

## Cari Analiz

- Backend multi-module Spring Boot mikroservisleri, Docker Compose, Liquibase,
  local seed data, Swagger/OpenAPI, Kafka outbox, MinIO, Redis, Prometheus ve
  Grafana ile artiq qurulub.
- Backend local demo user parolu: `Test1234!`.
- Frontend Next.js 15, React Query, Zustand, Axios, Zod ve real auth sehifeleri
  ile qurulub. `npm.cmd run typecheck` kecdi.
- Frontend portal sehifeleri hele catch-all route-lar ve `useMockQuery` uzerinde
  mock data gosterir.
- En kritik runtime uyghunsuzluqlar:
  - Frontend login `{ email, password }` gonderir ve response-da `user.role`
    gozleyir; backend `LoginRequest(identifier, password)` qebul edir ve
    `TokenResponse(accessToken, refreshToken, tokenType, expiresInSeconds)`
    qaytarir. User/role ucun `GET /api/v1/iam/me` cagrilmalidir.
  - Frontend password reset endpointleri `/reset/request` ve `/reset/confirm`,
    backend ise `/reset-request` ve `/reset-confirm` istifade edir.
  - Frontend API client-lerin bir qismi backend controller path/metodlari ile
    tam uygun deyil: policy cancel `PATCH + body`, health document upload
    presigned flow, payment mock endpoints, notification read endpointi, claim
    `resubmit/retry-payout` kimi movcud olmayan endpointler.
- Surətli MVP ucun external payment, SMTP/SMS ve OpenAI mecburi deyil:
  payment/notification mock isleyir, AI risk fallback mexanizmi var.

## Icra Sirasina Gore Promptlar

### Prompt 0 - Umumi Qaydalar

```text
Repo: C:\Users\Aga\Desktop\E-Health

Bu tasklarda meqsed yeni feature fantaziyasi yox, movcud SaglamOL backend ve
EH-FRONT frontend-i real islek MVP halina getirmekdir.

Qaydalar:
1. Movcud stack-i saxla: Next.js 15, TypeScript, Zustand, TanStack Query, Axios,
   Spring Boot 3.3.6, Gradle, Docker Compose.
2. Frontend endpointlerini backend controller-lere gore duzelt. Endpoint
   taxmin etme; lazim olsa controller ve DTO fayllarini oxu.
3. Mock data-ni yalniz fallback/dev empty state kimi saxla, portal sehifelerinde
   esas data real API-den gelsin.
4. Her promptdan sonra minimum yoxlama:
   - EH-FRONT: npm.cmd run typecheck
   - Deyisiklik backend-dedirse: .\gradlew.bat test ve ya uygun modul testi
5. Yeni external provider qosma. Local MVP mock/fallback ile islesin.
```

### Prompt 1 - Auth Contract Fix

```text
EH-FRONT auth qatini backend IAM contract-i ile tam uygunlasdir.

Backend faktlari:
- POST /api/v1/iam/login request: { identifier: string, password: string }
- POST /api/v1/iam/login/email request: { email: string, password: string }
- POST /api/v1/iam/login/phone request: { phoneNumber: string, password: string }
- TokenResponse: { accessToken, refreshToken, tokenType, expiresInSeconds }
- GET /api/v1/iam/me response: { userId, email, phoneNumber, roles: string[] }
- POST /api/v1/iam/register request: { email, phoneNumber?, password }
- Password reset:
  - POST /api/v1/iam/password/reset-request
  - POST /api/v1/iam/password/reset-confirm

Tapshiriqlar:
1. `EH-FRONT/src/lib/api/iam.api.ts` tiplerini backend DTO-lara uygunlasdir.
   `loginEmail`, `loginPhone`, `login`, `me`, `register`, `logout` methodlari
   yarat. `register` request-indan `role` sahesini sil.
2. `LoginPage`-de submit zamani email formasindan `/login/email` cagir.
   Token alinan kimi `setTokens` et, sonra `iamApi.me()` cagir ve `roles`
   array-den primary role sec.
3. Primary role secimi ucun helper yarat:
   `ADMIN > SYSTEM > HOSPITAL_ADMIN > HOSPITAL_STAFF > INSURANCE_ADMIN >
   INSURANCE_STAFF > AGENT > DOCTOR > PATIENT`.
4. `AuthUser` modelini backend-e uygunlasdir:
   `{ userId, email, phoneNumber?, roles, role, fullName? }`.
   `fullName` yoxdursa email prefix-i fallback kimi goster.
5. Login ugurlu olanda `saglamol_role` cookie-sini primary role ile set et ve
   `getPortalForRole(role)` ile dashboard-a redirect et.
6. `ApplyPage`-de:
   - PATIENT ucun real `/api/v1/iam/register` cagir: email, phoneNumber, password.
   - HOSPITAL ve INSURANCE ucun backend-de public application endpoint olmadigini
     UI-da net gosteren success state saxla, amma IAM register cagirib yalnis role
     yaratma.
7. `password.api.ts` endpointlerini `/reset-request` ve `/reset-confirm` ile
   duzelt.
8. Error mapping:
   - 400 validation, 401/403 login error, 409 duplicate, network unavailable.
9. Yoxla: `npm.cmd run typecheck`.
```

### Prompt 2 - Frontend API Client Alignment

```text
EH-FRONT/src/lib/api qatini backend controller path/metodlari ile uygunlasdir.

Movcud fayllari oxu:
- SaglamOL/services/**/controller/*.java
- SaglamOL/services/**/dto/request/*.java
- SaglamOL/services/**/dto/response/*.java
- EH-FRONT/src/lib/api/*.ts

Tapshiriqlar:
1. `password.api.ts` Prompt 1-de duzeldilibse tekrar pozma.
2. `policies.api.ts`:
   - `cancel` ve `suspend` `PATCH /api/v1/policies/{id}/cancel|suspend`
     ve body `{ reason }` gondersin.
   - `getMine`, `getById`, `search`, `issue`, `checkEligibility` qalsin.
3. `health-records.api.ts`:
   - `getMyRecords(page,size)` Page<HealthRecordResponse> qaytarsin.
   - document upload backend flow-a uygun olsun:
     `POST /api/v1/health-records/{id}/documents/uploads`
     sonra client presigned URL-e PUT edir,
     sonra `PUT /api/v1/health-records/{id}/documents/{documentId}/confirm`.
   - direct multipart endpoint yaratma, backend-de yoxdur.
4. Yeni API client fayllari yarat:
   - `notifications.api.ts`: `/notifications/my`, `/notifications/{id}`,
     `/notifications/by-company/{companyId}`, `/notifications/templates`.
   - `fraud.api.ts`: `POST /fraud/claims/{claimId}/check`,
     `GET /fraud/claims/{claimId}`, summaries.
   - `ai-risk.api.ts`: `POST /ai-risk/claims/{claimId}/assess`,
     `GET /ai-risk/claims/{claimId}`, company summary.
   - `insurance-products.api.ts`: `/api/v1/insurance-products`,
     status patch, coverage rules.
   - `invoices.api.ts`: `/api/v1/invoices`, by-company, by-hospital,
     issue, mark-paid, cancel.
   - `provider-contracts.api.ts`: `/api/v1/provider-contracts`,
     by-company, by-hospital, terminate.
5. `payments.api.ts`:
   - `createPolicyPremium`: POST `/api/v1/payments/policy-premium`
   - `claimPayout`: POST `/api/v1/payments/claim-payout`
   - `completeMock`, `failMock`, `refundMock`
   - `byPolicy`, `byClaim`, `byCompany`, `byHospital`, `mine`, `getById`
6. `claims.api.ts`:
   - movcud backend endpointlerini saxla.
   - `resubmit` ve `retryPayout` methodlarini sil ve ya optional TODO kimi
     comment et, cunki backend-de public endpointleri yoxdur.
7. `admin.api.ts`:
   - user get/list/search/status/role methods.
   - hospital create/list/get/update/status/branches/staff/doctors.
   - insurance company create/list/get/update/status/staff.
8. `index.ts` yeni export-lari elave et. `health-records.api.ts` export-u da
   elave olunsun.
9. `types.ts`-de Page, ErrorResponse ve lazimli request/response tipleri real
   DTO-lara yaxinlasdirilsin. Lazim olmayan `unknown`-lari minimuma endir.
10. Yoxla: `npm.cmd run typecheck`.
```

### Prompt 3 - Real React Query Hooks

```text
EH-FRONT/src/lib/hooks qovlugunda real TanStack Query hook-lari yarat ve
`useMockQuery` asiliqlarini portal sehifelerinden cixar.

Yarad:
- useAuth.ts: me/logout helpers
- useClaims.ts
- usePolicies.ts
- usePayments.ts
- useProfiles.ts
- useHealthRecords.ts
- useNotifications.ts
- useFraud.ts
- useAiRisk.ts
- useInsuranceProducts.ts
- useInvoices.ts
- useProviderContracts.ts
- useAdmin.ts

Her hook:
1. `queryKey` stabil ve domain esasli olsun.
2. `enabled` parametrini desteklesin.
3. Mutations ugurlu olanda uygun query-lari invalidate etsin.
4. Loading/error/empty state-lere data versin.
5. API client response-unun `.data` hissesi qaytarilsin.

Minimum yoxlama:
- `npm.cmd run typecheck`
- `PortalPage` artiq `useMockQuery` import etmesin.
```

### Prompt 4 - Portal Pages: Mock Catch-All-dan Real MVP-ye

```text
Frontend portal routelarini real API data ile isleyen MVP sehifelere cevir.

Movcud problem:
- patient/hospital/insurance/admin route-lari `[[...slug]]/page.tsx` ile
  `PortalPage`-e dusur.
- `PortalPage` `useMockQuery` ve `buildPageModel` ile statik data gosterir.

Meqsed:
Catch-all route-lari ya sil, ya da yalniz legacy redirect kimi saxla. Asagidaki
sehifeler real React Query hook-lari ile islesin:

Patient:
- /patient/dashboard
- /patient/profile
- /patient/policies
- /patient/policies/[policyId]
- /patient/claims
- /patient/claims/new
- /patient/claims/[claimId]
- /patient/health-records
- /patient/health-records/[recordId]
- /patient/payments
- /patient/notifications
- /patient/settings

Hospital:
- /hospital/dashboard
- /hospital/claims
- /hospital/claims/new
- /hospital/health-records
- /hospital/health-records/[recordId]
- /hospital/documents/upload
- /hospital/doctors
- /hospital/staff
- /hospital/branches

Insurance:
- /insurance/dashboard
- /insurance/claims
- /insurance/claims/[claimId]
- /insurance/claims/[claimId]/review
- /insurance/policies
- /insurance/policies/issue
- /insurance/products
- /insurance/products/[productId]
- /insurance/payments
- /insurance/payments/payout/new
- /insurance/invoices
- /insurance/fraud
- /insurance/risk/[claimId]
- /insurance/contracts
- /insurance/staff
- /insurance/agents
- /insurance/company

Admin:
- /admin/dashboard
- /admin/users
- /admin/users/[userId]
- /admin/companies
- /admin/companies/[companyId]
- /admin/hospitals
- /admin/hospitals/[hospitalId]
- /admin/patients
- /admin/notifications/templates

Her sehifede:
1. `"use client"` yalniz lazim olan componentlerde olsun.
2. Real hook-dan data gelsin.
3. Loading skeleton, error state, empty state olsun.
4. Existing UI/design system qorunsun.
5. ID lazim olan portal sehifelerde ID-ni auth user scope/profile API-den al.
6. `npm.cmd run typecheck` kecsin.
```

### Prompt 5 - Backend Small Gaps for Full Frontend

```text
Frontend-in tam interaktiv olmasi ucun backend-de yalniz kicik, movcud domain-e
uygun endpoint bosluqlarini doldur. Boyuk yeni servis yaratma.

1. Notification read state:
   - `PATCH /notifications/{id}/read` ve ya `POST /notifications/{id}/read`
   - current user ownership check
   - `GET /notifications/my/unread-count`
   - service + tests

2. Claim resubmit:
   - `POST /api/v1/claims/{claimId}/resubmit`
   - yalniz `NEEDS_MORE_DOCUMENTS` statusundan submit axinina qayitsin
   - ownership/scope check, status history, event publish
   - service tests

3. Claim payout retry:
   - eger claim status modelinde `PAYOUT_FAILED` desteklenirse:
     `POST /api/v1/claims/{claimId}/retry-payout`
   - yalniz reviewer/insurance scope icaze alsin
   - payment event/command tekrar yaransin
   - service tests
   - eger domain bunu hele istemirse, frontend method/button-larini gizlet.

4. Public organization applications:
   - Bu MVP ucun mecburi deyil. Hospital/insurance apply UI request qebul
     ede biler, amma real backend workflow sonrakidir.

Yoxlama:
- `.\gradlew.bat test`
- Uyghun service testleri ayrica kecsin.
```

### Prompt 6 - Frontend Docker and Compose

```text
EH-FRONT-i production-ready Docker ile SaglamOL docker-compose stack-ine qos.

Tapshiriqlar:
1. `EH-FRONT/next.config.ts`-e `output: "standalone"` elave et.
2. `EH-FRONT/src/app/api/health/route.ts` yarat:
   GET `{ status: "ok", timestamp: Date.now() }`.
3. `EH-FRONT/Dockerfile` yarat:
   - node:20-alpine deps stage
   - npm ci
   - builder stage: npm run build
   - runner stage: standalone output, non-root user
   - expose 3000
   - healthcheck `/api/health`
4. `EH-FRONT/.dockerignore` yarat:
   node_modules, .next, .env.local, .git, npm debug logs.
5. `SaglamOL/docker-compose.yml`-e `frontend` service elave et:
   - context `../EH-FRONT`
   - host port `3001:3000` (Grafana 3000-dadir)
   - `NEXT_PUBLIC_API_URL=http://api-gateway:8080`
   - depends_on api-gateway healthy
   - same `saglamol-network`
6. `CORS_ALLOWED_ORIGINS`-e `http://localhost:3001` elave olundugunu yoxla.
7. Yoxla:
   - `npm.cmd run typecheck`
   - `npm.cmd run build`
   - `docker compose config --quiet`
```

### Prompt 7 - Backend Smoke Test

```text
SaglamOL backend stack-in local demo-ready oldugunu yoxla ve problem varsa fix et.

Isle:
1. `cd SaglamOL`
2. `.\\gradlew.bat clean build`
3. `.\\gradlew.bat bootJar`
4. `docker compose config --quiet`
5. `docker compose up -d --build`
6. `docker compose ps`
7. Health endpointleri:
   - 8080 api-gateway
   - 8081 iam-service
   - 8082 user-profile-service
   - 8083 policy-service
   - 8084 claim-service
   - 8085 health-record-service
   - 8086 ai-risk-service
   - 8087 fraud-detection-service
   - 8088 notification-service
   - 8089 payment-service
   - 8761 discovery-server
   - 8888 config-server

Alternativ:
`powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\docker-smoke-test.ps1 -ResetVolumes -HealthRetries 60 -HealthDelaySeconds 15`

Failure olsa:
- son 150 log setrini oxu
- env/secret, db migration, service discovery ve Kafka readiness problemlerni
  konkret fix et
- tekrar smoke test et.
```

### Prompt 8 - End-to-End Demo Flow

```text
Frontend + backend birlikde E2E demo axinini yoxla.

Hazirliq:
- Backend: `SaglamOL/docker compose up -d --build`
- Frontend dev: `EH-FRONT/npm.cmd run dev`
- Frontend env: `NEXT_PUBLIC_API_URL=http://localhost:8080`
- Demo parol: `Test1234!`

Login testleri:
1. `patient@saglamol.az` -> /patient/dashboard
2. `agent@saglamol.az` -> /insurance/dashboard
3. `hospital-admin@saglamol.az` -> /hospital/dashboard
4. `admin@saglamol.az` -> /admin/dashboard

Patient axini:
1. /patient/policies real policy list
2. /patient/health-records create/list/detail
3. document upload presigned flow
4. /patient/claims/new create draft -> add item -> attach document -> submit
5. /patient/notifications real notifications

Insurance/agent axini:
1. /insurance/claims claim search
2. claim detail
3. start review
4. approve/reject/more-documents
5. fraud ve AI risk detail
6. payment payout mock status

Admin axini:
1. users list/search/status/roles
2. companies list/create/update/status
3. hospitals list/create/update/status/branches/staff
4. notification templates list/create/update/status

Her addimda yoxla:
- Network request URL backend controller ile uygundur
- 2xx response gelir
- 401 olarsa token refresh isleyir
- CORS xetasi yoxdur
- UI loading/error/empty state normaldir
```

## Production-da Qosulacaq External-lar

Local MVP ucun bunlar mecburi deyil; mock/fallback ile islemek olar. Production
ucun ise bunlar secilib qosulmalidir:

1. Payment provider
   - Premium payment, payout, refund, webhook, idempotency key.
   - Hazirda `MockPaymentGatewayClient` var.
   - Real adapter ucun `PaymentGatewayClient` interface cixarmaq meslehetdir.

2. Notification provider
   - SMTP/email provider: SES, SendGrid, Mailgun ve ya lokal SMTP.
   - SMS provider: lokal operator aggregator, Twilio ve ya alternativ.
   - Hazirda `EmailMockSender`, `SmsMockSender`, `InAppNotificationSender` var.

3. AI provider
   - OpenAI-compatible endpoint ve ya Vertex-compatible gateway.
   - Env: `AI_API_KEY`, `AI_BASE_URL`, `AI_MODEL`, `AI_TIMEOUT_SECONDS`.
   - Provider yoxdursa fallback scoring islemelidir.

4. Object storage
   - Local MinIO qalir.
   - Production ucun managed S3/MinIO, bucket policy, lifecycle, backup.

5. Domain, TLS, reverse proxy
   - Frontend domain, API domain, HTTPS certificate, CORS origins.

6. Secret management
   - `.env.production` lokal fayl kimi yox, Vault / cloud secret manager.
   - JWT secret, internal service secret, DB password, provider API keys.

7. Managed infrastructure
   - PostgreSQL backups, Kafka retention/DLT, Redis persistence policy.
   - Prometheus/Grafana dashboard provisioning ve alerting.

8. Optional healthcare integrations
   - FHIR/HL7/HIS integration, OCR/document extraction, KYC/e-signature.

