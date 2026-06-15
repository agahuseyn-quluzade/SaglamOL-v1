# SaglamOL — Deploya Hazırlıq Promptları

Aşağıdakı promptları ardıcıl icra etməklə həm **backend (SaglamOL)**, həm **frontend (EH-FRONT)** layihəsini tam deploy-ready vəziyyətə gətirmək olar. Hər prompt müstəqil bir iş vahididir.

> [!IMPORTANT]
> Promptlar sıra ilə icra olunmalıdır — hər sonrakı prompt əvvəlkinin nəticəsinə əsaslanır.

---

## FASE 1 — Frontend: Real Auth İnteqrasiyası

### Prompt 1.1 — Login səhifəsini real backend-ə qoşmaq

```
EH-FRONT/src/app/(public)/login/page.tsx faylında hazırda demoAccounts adlı hardcoded mock
auth sistemi var. Bu mock sistemi tamamilə silmək və əvəzinə real backend API çağırışı ilə 
əvəzləmək lazımdır.

Tələblər:
1. demoAccounts obyektini və bütün mock login məntiğini sil.
2. Form submit handler-ində lib/api/iam.api.ts faylındakı iamApi.login() funksiyasını çağır.
3. Backend-dən gələn response-dan accessToken, refreshToken və user məlumatlarını 
   useAuthStore-a yaz (setTokens + setUser).
4. saglamol_role cookie-sini backend-dən gələn user.role ilə set et.
5. Uğurlu login-dən sonra getPortalForRole(user.role) ilə uyğun portal dashboard-a redirect et.
6. Error handling əlavə et:
   - Backend 401/403 qaytarsa "Email və ya şifrə yanlışdır" xətası göstər
   - Network error-da "Server əlçatmazdır" xətası göstər
   - Form-da error message-ı user-ə göstərmək üçün state əlavə et
7. Submit zamanı loading state əlavə et (button disabled + spinner/text dəyişikliyi).
8. Mövcud password-reset link-ini və register/apply link-ini saxla.

Backend endpoint: POST /api/v1/iam/login
Request body: { email: string, password: string }
Response: { accessToken: string, refreshToken: string, user: { email, fullName, role } }
```

### Prompt 1.2 — Register/Apply səhifəsini backend-ə qoşmaq

```
EH-FRONT/src/app/(public)/apply/page.tsx faylında müraciət formu var, amma form submit 
handler-i yoxdur — heç bir data backend-ə göndərilmir.

Tələblər:
1. React Hook Form + Zod validation əlavə et (login page-dəki kimi pattern).
2. Zod schema-da bu field-ları validate et:
   - applicantType: enum("PATIENT", "HOSPITAL", "INSURANCE") — required
   - name: string, min 2 simvol — required
   - email: string, email format — required
   - phone: string, +994 ilə başlamalı — required
   - identifier: string (FIN/VÖEN) — optional
   - note: string — optional
3. Submit handler-ində iamApi.register() çağır. Əgər backend-də apply üçün ayrıca endpoint 
   varsa, lib/api/ qovluğunda yeni apply.api.ts yarat.
4. Uğurlu submit-dən sonra "Müraciətiniz qəbul edildi" success mesajı göstər və login-ə 
   yönləndir.
5. Error handling: duplicate email, validation error-lar backend-dən gələn mesajla göstərilsin.
6. Loading state əlavə et.
```

### Prompt 1.3 — Password Reset səhifələrini backend-ə qoşmaq

```
EH-FRONT/src/app/(public)/password-reset/ qovluğunda request və confirm səhifələri var.
Bunları lib/api/password.api.ts faylındakı funksiyalara qoşmaq lazımdır.

Tələblər:
1. password-reset/request/page.tsx:
   - Email input + Zod validation əlavə et
   - Submit-də passwordApi.requestReset(email) çağır
   - Uğurlu response-da "Email-inizə sıfırlama linki göndərildi" mesajı göstər
   - Error handling əlavə et

2. password-reset/confirm/page.tsx:
   - URL-dən token query parametrini oxu (useSearchParams)
   - Yeni şifrə + şifrə təkrarı form-u yarat (Zod ilə min 8 simvol, match validation)
   - Submit-də passwordApi.confirmReset(token, newPassword) çağır
   - Uğurlu response-da login-ə redirect et
   - Token invalid/expired olsa error göstər

Backend endpoints:
- POST /api/v1/iam/password/reset/request → { email }
- POST /api/v1/iam/password/reset/confirm → { token, newPassword }
```

---

## FASE 2 — Frontend: Çatışan API Client-lərin Yaradılması

### Prompt 2.1 — Çatışan bütün API client-ləri yazmaq

```
EH-FRONT/src/lib/api/ qovluğunda mövcud API client-lər var (iam, claims, policies, payments, 
profiles, password, admin). Lakin backend-dəki aşağıdakı servislər üçün API client yazmaq 
lazımdır.

Mövcud pattern-ə uyğun olaraq hər servis üçün ayrıca fayl yarat. Hər fayda apiClient 
import et və named export ilə API obyekti export et.

Yaradılacaq fayllar:

1. health-records.api.ts
   Backend: /api/v1/health-records
   Endpoints:
   - getMyRecords() → GET /api/v1/health-records/my
   - getById(id) → GET /api/v1/health-records/{id}
   - create(data) → POST /api/v1/health-records
   - addDocument(healthRecordId, file) → POST /api/v1/health-records/{id}/documents 
     (multipart/form-data)
   - getDocuments(healthRecordId) → GET /api/v1/health-records/{id}/documents

2. notifications.api.ts
   Backend: /notifications (QEYD: bu servisdə /api/v1/ prefix yoxdur)
   Endpoints:
   - getMyNotifications(page, size) → GET /notifications/my
   - markAsRead(id) → PATCH /notifications/{id}/read
   - getTemplates() → GET /notifications/templates
   - createTemplate(data) → POST /notifications/templates

3. fraud.api.ts
   Backend: /fraud (QEYD: /api/v1/ prefix yoxdur)
   Endpoints:
   - getCompanySummary(companyId) → GET /fraud/companies/{companyId}/summary
   - getHospitalSummary(hospitalId) → GET /fraud/hospitals/{hospitalId}/summary
   - getClaimFraudScore(claimId) → GET /fraud/claims/{claimId}

4. ai-risk.api.ts
   Backend: /ai-risk (QEYD: /api/v1/ prefix yoxdur)
   Endpoints:
   - analyzeClaimRisk(claimId) → GET /ai-risk/claims/{claimId}

5. insurance-products.api.ts
   Backend: /api/v1/insurance-products
   Endpoints:
   - getAll(params) → GET /api/v1/insurance-products
   - getById(id) → GET /api/v1/insurance-products/{id}
   - create(data) → POST /api/v1/insurance-products
   - update(id, data) → PUT /api/v1/insurance-products/{id}
   - getCoverageRules(productId) → GET /api/v1/insurance-products/{productId}/coverage-rules
   - addCoverageRule(productId, data) → POST /api/v1/insurance-products/{productId}/coverage-rules

6. invoices.api.ts
   Backend: /api/v1/invoices
   Endpoints:
   - getById(id) → GET /api/v1/invoices/{id}
   - getByCompany(companyId) → GET /api/v1/invoices/by-company?companyId=
   - getByHospital(hospitalId) → GET /api/v1/invoices/by-hospital?hospitalId=
   - create(data) → POST /api/v1/invoices

7. provider-contracts.api.ts
   Backend: /api/v1/provider-contracts
   Endpoints:
   - getByCompany(companyId) → GET /api/v1/provider-contracts/by-company/{companyId}
   - getById(id) → GET /api/v1/provider-contracts/{id}
   - create(data) → POST /api/v1/provider-contracts

Bunlardan sonra index.ts faylına bütün yeni export-ları əlavə et.

Mövcud type-lar üçün types.ts faylına lazımi DTO tipləri əlavə et — hər endpoint üçün 
Request və Response tipləri olsun.
```

### Prompt 2.2 — Mövcud API client-lərə çatışan endpoint-ləri əlavə etmək

```
Mövcud API client fayllarında backend-dəki bəzi endpoint-lər əksik qalıb. Bunları əlavə et:

1. profiles.api.ts-ə əlavə et:
   - createPatient(data) → POST /api/v1/profiles/patients
   - getPatientById(id) → GET /api/v1/profiles/patients/{id}
   - updatePatient(id, data) → PUT /api/v1/profiles/patients/{id}
   - createDoctor(data) → POST /api/v1/profiles/doctors
   - getDoctorMe() → GET /api/v1/profiles/doctors/me
   - getDoctorById(id) → GET /api/v1/profiles/doctors/{id}
   - updateDoctor(id, data) → PUT /api/v1/profiles/doctors/{id}
   - searchDoctors(q) → GET /api/v1/profiles/doctors/search?q=
   - createAgent(data) → POST /api/v1/profiles/agents
   - getAgentMe() → GET /api/v1/profiles/agents/me
   - getAgentById(id) → GET /api/v1/profiles/agents/{id}
   - updateAgent(id, data) → PUT /api/v1/profiles/agents/{id}
   - searchAgents(q) → GET /api/v1/profiles/agents/search?q=

2. admin.api.ts-ə əlavə et:
   - createHospital(data) → POST /api/v1/profiles/hospitals
   - getHospitalById(id) → GET /api/v1/profiles/hospitals/{id}
   - updateHospital(id, data) → PUT /api/v1/profiles/hospitals/{id}
   - updateHospitalStatus(id, status) → PATCH /api/v1/profiles/hospitals/{id}/status
   - createCompany(data) → POST /api/v1/insurance-companies
   - getCompanyById(id) → GET /api/v1/insurance-companies/{id}
   - updateCompany(id, data) → PUT /api/v1/insurance-companies/{id}
   - updateCompanyStatus(id, status) → PATCH /api/v1/insurance-companies/{id}/status
   - getCompanyStaff(companyId) → GET /api/v1/insurance-companies/{companyId}/staff
   - addCompanyStaff(companyId, data) → POST /api/v1/insurance-companies/{companyId}/staff
   - getHospitalBranches(hospitalId) → GET /api/v1/profiles/hospitals/{hospitalId}/branches
   - addHospitalBranch(hospitalId, data) → POST /api/v1/profiles/hospitals/{hospitalId}/branches
   - getHospitalStaff(hospitalId) → GET /api/v1/profiles/hospitals/{hospitalId}/staff
   - addHospitalStaff(hospitalId, data) → POST /api/v1/profiles/hospitals/{hospitalId}/staff
```

---

## FASE 3 — Frontend: Mock Data-nı Real API-yə Keçirmək

### Prompt 3.1 — React Query hook-ları yaratmaq

```
EH-FRONT/src/lib/hooks/ qovluğunda hazırda yalnız useMockQuery.ts var və mock data qaytarır.
Hər backend servisi üçün ayrıca real React Query hook faylı yaratmaq lazımdır.

Yaradılacaq fayllar (src/lib/hooks/ qovluğunda):

1. useClaims.ts
   - useMyClaimsQuery(page, size) → claimsApi.getMyClaims()
   - useClaimByIdQuery(claimId) → claimsApi.getById()
   - useClaimSearchQuery(filters) → claimsApi.search()
   - useCreateClaimMutation() → claimsApi.create()
   - useSubmitClaimMutation() → claimsApi.submit()
   - useAddClaimItemMutation() → claimsApi.addItem()
   - useClaimReviewMutation(action) → claimsApi.review[action]()

2. usePolicies.ts
   - useMyPoliciesQuery() → policiesApi.getMine()
   - usePolicyByIdQuery(id) → policiesApi.getById()
   - usePolicySearchQuery(params) → policiesApi.search()
   - useIssuePolicyMutation() → policiesApi.issue()
   - useCancelPolicyMutation() → policiesApi.cancel()
   - useEligibilityCheckMutation() → policiesApi.checkEligibility()

3. usePayments.ts
   - useMyPaymentsQuery() → paymentsApi.mine()
   - usePaymentsByPolicyQuery(policyId) → paymentsApi.byPolicy()
   - usePaymentsByCompanyQuery(companyId) → paymentsApi.byCompany()
   - useClaimPayoutMutation() → paymentsApi.claimPayout()

4. useProfiles.ts
   - usePatientMeQuery() → profilesApi.patientMe()
   - useSearchPatientsQuery(q) → profilesApi.searchPatients()
   - useHospitalDoctorsQuery(id) → profilesApi.hospitalDoctors()
   - useHospitalStaffQuery(id) → profilesApi.hospitalStaff()
   - useHospitalBranchesQuery(id) → profilesApi.hospitalBranches()

5. useHealthRecords.ts
   - useMyHealthRecordsQuery() → healthRecordsApi.getMyRecords()
   - useHealthRecordByIdQuery(id) → healthRecordsApi.getById()
   - useUploadDocumentMutation() → healthRecordsApi.addDocument()

6. useNotifications.ts
   - useMyNotificationsQuery(page) → notificationsApi.getMyNotifications()
   - useMarkAsReadMutation() → notificationsApi.markAsRead()

7. useFraud.ts
   - useFraudCompanySummaryQuery(companyId) → fraudApi.getCompanySummary()
   - useFraudHospitalSummaryQuery(hospitalId) → fraudApi.getHospitalSummary()
   - useClaimFraudScoreQuery(claimId) → fraudApi.getClaimFraudScore()

8. useAdmin.ts
   - useUsersQuery() → adminApi.users()
   - useSearchUsersQuery(q) → adminApi.searchUsers()
   - useCompaniesQuery() → adminApi.companies()
   - useHospitalsQuery() → adminApi.hospitals()

Hər hook:
- React Query useQuery/useMutation istifadə etsin
- Error handling ilə olsun
- queryKey-lər mənalı olsun: ["claims", "my", page] kimi
- enabled parametri ilə conditional fetch dəstəkləsin
```

### Prompt 3.2 — Domain komponentləri real data-ya qoşmaq

```
EH-FRONT/src/components/domain/ qovluğundakı komponentlər hazırda hamısı mock/statik 
data göstərir. Bunları real React Query hook-larına qoşmaq lazımdır.

Hər domain qovluğu üçün:

1. domain/claim/ (6 komponent):
   - ClaimCreationWizard.tsx: useCreateClaimMutation + useAddClaimItemMutation istifadə etsin.
     Multi-step form: step 1 → policyId seçimi (useMyPoliciesQuery), step 2 → item əlavəsi,
     step 3 → submit (useSubmitClaimMutation)
   - ClaimStatusTimeline.tsx: useClaimByIdQuery-dən statusHistory göstərsin
   - ClaimDocumentList.tsx: useClaimByIdQuery-dən documents array render etsin
   - ClaimReviewPanel.tsx: useClaimReviewMutation ilə approve/reject/more-docs əməliyyatları
   - ClaimRiskBanner.tsx: useClaimFraudScoreQuery ilə fraud score göstərsin
   - ClaimFraudBanner.tsx: useFraudCompanySummaryQuery ilə fraud summary

2. domain/policy/ (5 komponent):
   - PolicyCard.tsx: usePolicyByIdQuery-dən data göstərsin
   - PolicyLimitGauge.tsx: policy-dən remainingLimit/totalLimit göstərsin
   - EligibilityCheckForm.tsx: useEligibilityCheckMutation istifadə etsin
   - IssuePolicyForm.tsx: useIssuePolicyMutation istifadə etsin
   - PolicyStatusBadge.tsx: status prop-a əsasən rəng göstərsin (artıq düzgündür)

3. domain/payment/ (4 komponent):
   - PaymentTimeline.tsx: useMyPaymentsQuery / usePaymentsByPolicyQuery istifadə etsin
   - PayoutForm.tsx: useClaimPayoutMutation istifadə etsin
   - PremiumPaymentForm.tsx: ödəniş formu (frontend-only, payment gateway əlavə edilə bilər)
   - PaymentStatusBadge.tsx: status prop

4. domain/health-record/ (4 komponent):
   - HealthRecordCard.tsx: useMyHealthRecordsQuery / useHealthRecordByIdQuery
   - TreatmentList.tsx: health record-dan treatment-ları list et
   - DocumentViewer.tsx: document URL-ə bağlansın
   - DocumentUploader.tsx: useUploadDocumentMutation + file input

5. domain/notification/ (3 komponent):
   - NotificationBell.tsx: useMyNotificationsQuery ilə sayğac
   - NotificationList.tsx: useMyNotificationsQuery ilə list render
   - NotificationItem.tsx: useMarkAsReadMutation ilə mark as read

6. domain/fraud/ (3 komponent):
   - FraudSummaryCard.tsx: useFraudCompanySummaryQuery
   - FraudScoreGauge.tsx: useClaimFraudScoreQuery ilə vizual gauge
   - FraudSignalList.tsx: fraud detail list

7. domain/dashboard/ (5 komponent):
   - StatCard.tsx: prop-based (olduğu kimi), data dashboard page-dən gəlsin
   - ClaimPipelineChart.tsx: claim status aggregation
   - RevenueChart.tsx: payment aggregation data
   - FraudHeatmap.tsx: fraud summary data
   - RecentActivityFeed.tsx: notification/activity feed

Hər komponentdə:
- Loading skeleton əlavə et (useQuery-nin isLoading state-i)
- Error state göstər (useQuery-nin isError state-i)  
- Empty state göstər (data boş olduqda)
```

---

## FASE 4 — Frontend: Catch-All Routing-dən Ayrı Səhifələrə Keçmək

### Prompt 4.1 — Patient portal-ın ayrı page-lərə bölünməsi

```
Hazırda EH-FRONT/src/app/(portal)/patient/[[...slug]]/page.tsx tək bir catch-all route ilə
bütün patient səhifələrini render edir. Bunu ayrı-ayrı page.tsx fayllarına bölmək lazımdır.

Yaradılacaq fayl strukturu:
src/app/(portal)/patient/
├── dashboard/
│   └── page.tsx         ← Dashboard: StatCards + Charts
├── profile/
│   └── page.tsx         ← Patient profile view/edit
├── policies/
│   ├── page.tsx         ← Policy list (useMyPoliciesQuery)
│   └── [policyId]/
│       └── page.tsx     ← Policy detail (usePolicyByIdQuery)
├── claims/
│   ├── page.tsx         ← Claims list (useMyClaimsQuery + pagination)
│   ├── new/
│   │   └── page.tsx     ← ClaimCreationWizard
│   └── [claimId]/
│       └── page.tsx     ← Claim detail
├── health-records/
│   ├── page.tsx         ← Health records list
│   └── [recordId]/
│       └── page.tsx     ← Record detail + documents
├── payments/
│   └── page.tsx         ← Payment history
├── notifications/
│   └── page.tsx         ← Notification list
└── settings/
    └── page.tsx         ← Password change form

Hər page.tsx:
- "use client" olsun
- Uyğun React Query hook-ları istifadə etsin  
- Next.js metadata export etsin (generateMetadata ilə)
- Loading state üçün loading.tsx əlavə etsin
- Lazım olan domain komponentləri import etsin

[[...slug]] catch-all route-u silmək. PortalPage komponentini refactor edib hər page-ə 
uyğun hissəsini ayırmaq.
```

### Prompt 4.2 — Hospital portal-ın ayrı page-lərə bölünməsi

```
Eyni pattern-i hospital portalı üçün tətbiq et.

Yaradılacaq fayl strukturu:
src/app/(portal)/hospital/
├── dashboard/page.tsx      ← Dashboard
├── claims/
│   ├── page.tsx            ← Hospital claims list (claimsApi.search({hospitalId}))
│   └── new/page.tsx        ← Claim creation for patient
├── health-records/
│   ├── page.tsx            ← Health records management
│   └── [recordId]/page.tsx ← Record detail
├── documents/
│   └── upload/page.tsx     ← Document upload
├── doctors/page.tsx        ← Doctor list (profilesApi.hospitalDoctors)
├── staff/page.tsx          ← Staff list (profilesApi.hospitalStaff)
└── branches/page.tsx       ← Branch list (profilesApi.hospitalBranches)

Hospital-a xas: hospitalId-ni auth store-dakı user-ə bağlı profildan almaq lazımdır.
```

### Prompt 4.3 — Insurance portal-ın ayrı page-lərə bölünməsi

```
Insurance portalı üçün tətbiq et.

src/app/(portal)/insurance/
├── dashboard/page.tsx
├── claims/
│   ├── page.tsx                  ← Claim search (companyId ilə)
│   └── [claimId]/
│       ├── page.tsx              ← Claim detail + review
│       └── review/page.tsx       ← ClaimReviewPanel
├── policies/
│   ├── page.tsx                  ← Policy search
│   └── issue/page.tsx            ← EligibilityCheck + IssuePolicy forms
├── products/
│   ├── page.tsx                  ← Insurance products list
│   └── [productId]/page.tsx      ← Product detail + coverage rules
├── payments/
│   ├── page.tsx                  ← Payments list (by company)
│   └── payout/new/page.tsx       ← Payout form
├── invoices/page.tsx             ← Invoices list (by company)
├── fraud/page.tsx                ← Fraud summary + signals
├── risk/demo-claim/page.tsx      ← AI risk demo
├── contracts/page.tsx            ← Provider contracts
├── staff/page.tsx                ← Company staff
├── agents/page.tsx               ← Agent list
└── company/page.tsx              ← Company profile
```

### Prompt 4.4 — Admin portal-ın ayrı page-lərə bölünməsi

```
Admin portalı üçün tətbiq et.

src/app/(portal)/admin/
├── dashboard/page.tsx
├── users/
│   ├── page.tsx            ← User list + search (adminApi.users/searchUsers)
│   └── [userId]/page.tsx   ← User detail + status update
├── companies/
│   ├── page.tsx            ← Insurance companies list
│   └── [companyId]/page.tsx ← Company detail + staff
├── hospitals/
│   ├── page.tsx            ← Hospital list
│   └── [hospitalId]/page.tsx ← Hospital detail
├── patients/page.tsx       ← Patient search
└── notifications/
    └── templates/page.tsx  ← Notification templates management
```

---

## FASE 5 — Frontend: UX Polish və Error Handling

### Prompt 5.1 — Global error handling və toast sistemi

```
Frontend-ə global error handling və toast notification sistemi əlavə et.

1. Toast Komponenti yarat: src/components/ui/Toast.tsx
   - Success (yaşıl), error (qırmızı), warning (sarı), info (mavi) variant-ları
   - Auto-dismiss (5 saniyə) + manual close
   - Stack şəklində yuxarıdan göstərilsin
   - Animasiya: slide-in + fade-out

2. Toast Store yarat: src/lib/stores/toast.store.ts (Zustand)
   - addToast(message, type), removeToast(id)

3. Axios interceptor-ə global error handler əlavə et (src/lib/api/client.ts):
   - 400 → "Məlumatlar düzgün deyil" + backend details
   - 401 → auto-refresh (artıq var), failed refresh → "Sessiya bitib"
   - 403 → "Bu əməliyyata icazəniz yoxdur"
   - 404 → "Məlumat tapılmadı"
   - 409 → "Məlumat konflikti" + backend message
   - 500 → "Server xətası, zəhmət olmasa yenidən cəhd edin"
   - Network error → "Server əlçatmazdır"

4. React Query global error handler (src/components/layout/AppProviders.tsx):
   - QueryClient defaultOptions-da onError callback-ində toast.addToast çağır

5. Mutation-lar üçün success toast:
   - Create/update/delete əməliyyatlarından sonra success toast göstər
```

### Prompt 5.2 — Loading skeletons və empty states

```
Frontend-ə loading və empty state UI komponentləri əlavə et.

1. Skeleton komponentləri yarat: src/components/ui/Skeleton.tsx
   - SkeletonLine (text placeholder)
   - SkeletonCard (card-shaped placeholder)
   - SkeletonTable (table rows placeholder)
   - CSS pulse animation

2. Hər portal route üçün loading.tsx fayl yarat.
   Məs: src/app/(portal)/patient/claims/loading.tsx
   - SkeletonTable render etsin

3. EmptyState komponentini genişləndir (src/components/ui/EmptyState.tsx):
   - icon, title, description, action (button) prop-ları
   - Claim-lar boş → "Hələ claim yoxdur" + "Yeni claim yarat" button
   - Policies boş → "Hələ polisiniz yoxdur"

4. DataTable-a pagination əlavə et:
   - Page<T> tipinə uyğun prev/next buttonlar
   - Hazırkı səhifə göstəricisi
   - totalElements / totalPages göstərsin
```

### Prompt 5.3 — Responsive sidebar və dark mode persist

```
1. Sidebar responsive davranışını əlavə et (src/styles/layout.css + PortalShell.tsx):
   - Desktop (>1024px): sidebar açıq, 260px width
   - Tablet (768-1024px): sidebar collapse, yalnız ikon görünsün, hover-də açılsın
   - Mobile (<768px): sidebar gizli, menu button ilə overlay açılsın
   - Backdrop overlay mobile-da
   - Sidebar toggle button PortalShell-dəki Menu button-a bağlansın

2. UI Store-a persist əlavə et (src/lib/stores/ui.store.ts):
   - Zustand persist middleware əlavə et
   - localStorage key: "saglamol-ui"
   - theme dəyərini persist et
   - sidebarOpen-ı persist etmə (responsive-a əngəl olur)

3. Dark mode CSS variables (src/styles/variables.css):
   - [data-theme="dark"] selector-u əlavə et (əgər yoxdursa)
   - Bütün background, text, border, card rəngləri üçün dark variant
   - Transition: background-color 0.3s, color 0.3s
```

---

## FASE 6 — Frontend: Dockerize və Deploy Konfiqurasiyası

### Prompt 6.1 — Frontend Dockerfile və docker-compose əlavəsi

```
EH-FRONT layihəsi üçün production-ready Dockerfile yarat və SaglamOL/docker-compose.yml 
faylına frontend servisini əlavə et.

1. EH-FRONT/Dockerfile yarat:
   - Multi-stage build:
     Stage 1 (deps): node:20-alpine, package.json + package-lock.json COPY, npm ci
     Stage 2 (builder): source COPY, npm run build (Next.js standalone output)
     Stage 3 (runner): node:20-alpine, standalone output COPY, non-root user
   - next.config.ts-ə output: "standalone" əlavə et
   - PORT 3000 expose
   - ENV: NEXT_PUBLIC_API_URL, NODE_ENV=production
   - HEALTHCHECK: curl http://localhost:3000/api/health

2. EH-FRONT/src/app/api/health/route.ts yarat:
   - GET handler: { status: "ok", timestamp: Date.now() } qaytarsın

3. SaglamOL/docker-compose.yml faylına frontend servisi əlavə et:
   frontend:
     build:
       context: ../EH-FRONT
       dockerfile: Dockerfile
     image: saglamol/frontend:local
     ports:
       - "3001:3000"
     environment:
       NEXT_PUBLIC_API_URL: http://api-gateway:8080
     depends_on:
       api-gateway:
         condition: service_healthy
     healthcheck:
       test: ["CMD-SHELL", "curl -fsS http://localhost:3000/api/health > /dev/null"]
       interval: 15s
       timeout: 5s
       retries: 5
     networks:
       - saglamol-network

4. EH-FRONT/.dockerignore yarat:
   node_modules, .next, .env.local, .git
```

---

## FASE 7 — Backend: Deploy Hazırlığı

### Prompt 7.1 — Backend build və healthcheck yoxlaması

```
SaglamOL backend layihəsini build edib bütün servislərin ayağa qalxmasını yoxla.

1. Gradle build: 
   cd SaglamOL && ./gradlew clean build -x test
   (test-siz build — əvvəlcə build-in keçdiyini təsdiqlə)

2. Docker image build:
   docker-compose build

3. Servislərin ayağa qalxmasını yoxla:
   docker-compose up -d
   docker-compose ps  (bütün servislərin healthy olmasını gözlə)

4. Hər servisin actuator/health endpoint-ini yoxla:
   - discovery-server: http://localhost:8761/actuator/health
   - config-server: http://localhost:8888/actuator/health
   - api-gateway: http://localhost:8080/actuator/health
   - iam-service: http://localhost:8081/actuator/health
   - user-profile-service: http://localhost:8082/actuator/health
   - policy-service: http://localhost:8083/actuator/health
   - claim-service: http://localhost:8084/actuator/health
   - health-record-service: http://localhost:8085/actuator/health
   - ai-risk-service: http://localhost:8086/actuator/health
   - fraud-detection-service: http://localhost:8087/actuator/health
   - notification-service: http://localhost:8088/actuator/health
   - payment-service: http://localhost:8089/actuator/health

5. Infrastructure servisləri yoxla:
   - PostgreSQL: localhost:5432 (9 database yaradılıb?)
   - Redis: localhost:6379
   - Kafka: localhost:9092
   - MinIO: localhost:9001 (console)
   - Prometheus: localhost:9090
   - Grafana: localhost:3000

Problem tapsan, error log-ları göstər və həll et.
```

### Prompt 7.2 — Backend .env.production hazırlamaq

```
SaglamOL layihəsi üçün production-ready .env.production faylı hazırla. 

Mövcud .env faylında default/local dəyərlər var. Production üçün:

1. .env.production yaradılmalıdır (bu fayl gitignore-da olmalıdır):
   - POSTGRES_USER → güclü istifadəçi adı
   - POSTGRES_PASSWORD → minimum 32 simvol random şifrə
   - IAM_JWT_SECRET → minimum 64 simvol random secret
   - INTERNAL_SERVICE_SECRET → minimum 48 simvol random secret
   - MINIO_ROOT_USER → "minioadmin" deyil, güclü ad
   - MINIO_ROOT_PASSWORD → minimum 32 simvol random şifrə
   - MINIO_ACCESS_KEY / MINIO_SECRET_KEY → ayrıca güclü açarlar
   - AI_API_KEY → real OpenAI/Vertex AI key
   - GRAFANA_ADMIN_PASSWORD → güclü şifrə
   - LIQUIBASE_CONTEXTS → production

2. docker-compose.yml-dəki restart policy-ləri:
   - Bütün servislərdə restart: "no" → restart: unless-stopped
   
3. .gitignore-a əlavə et:
   - .env.production
   - .env (əgər yoxdursa)
```

### Prompt 7.3 — API Gateway CORS konfiqurasiyası

```
SaglamOL/infrastructure/api-gateway layihəsində frontend-in backend-ə CORS problemi 
olmadan qoşulması üçün CORS konfiqurasiyasını yoxla və düzəlt.

1. API Gateway-in application.yml və ya SecurityConfig faylında CORS konfiqurasiyası olmalıdır:
   - Allowed origins: 
     - development: http://localhost:3000, http://localhost:3001
     - production: https://saglamol.az (və ya actual domain)
   - Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
   - Allowed headers: Authorization, Content-Type, Accept
   - Exposed headers: Authorization
   - Allow credentials: true
   - Max age: 3600

2. Əgər Spring Cloud Gateway istifadə olunursa, global CORS filter əlavə et:
   spring.cloud.gateway.globalcors.cors-configurations
   
3. Əgər Spring Security istifadə olunursa, CorsConfigurationSource bean əlavə et.

4. Yoxla: frontend http://localhost:3001-dən backend http://localhost:8080-ə 
   cross-origin request göndərə bilir?
```

---

## FASE 8 — End-to-End Test və Final Yoxlama

### Prompt 8.1 — Tam inteqrasiya testi

```
Frontend və backend-in birlikdə işlədiyini end-to-end yoxla.

Test ssenarisi:

1. docker-compose up -d ilə bütün backend servisləri qaldır
2. Frontend-i npm run dev ilə localhost:3000-da qaldır (NEXT_PUBLIC_API_URL=http://localhost:8080)

3. Auth flow testi:
   - /apply səhifəsindən patient müraciəti göndər → backend-ə POST getdi?
   - /login səhifəsindən daxil ol → real token alındı?
   - Dashboard-a redirect olundu?
   - Logout et → /login-ə qayıtdı?
   - Token expire olduqda auto-refresh işlədi?

4. Patient portal testi:
   - /patient/policies → real policy list gəlir?
   - /patient/claims → real claim list gəlir?
   - /patient/claims/new → claim yaradıla bilir?
   - /patient/health-records → sağlamlıq qeydləri gəlir?
   - /patient/payments → ödəniş tarixi gəlir?
   - /patient/notifications → bildirişlər gəlir?

5. Hospital portal testi:
   - /hospital/claims → hospital claims gəlir?
   - /hospital/doctors → doctor list gəlir?
   - /hospital/documents/upload → fayl yüklənə bilir?

6. Insurance portal testi:
   - /insurance/claims → claim search işləyir?
   - /insurance/claims/{id} → claim detail + review panel
   - /insurance/policies/issue → polis çıxarıla bilir?
   - /insurance/fraud → fraud summary gəlir?

7. Admin portal testi:
   - /admin/users → user list gəlir?
   - /admin/companies → company list gəlir?

Hər addımda browser DevTools Network tabında:
- Request URL düzgündür?
- CORS xətası yoxdur?
- Response 200/201 gəlir?
- Data UI-da düzgün render olunur?

Error tapsan, konkret fix prompt-u hazırla.
```

---

## Qısa Xülasə

| Fase | Nə edilir | Təxmini fayl sayı |
|---|---|---|
| 1 | Auth inteqrasiyası (login, register, password-reset) | 3-4 fayl dəyişikliyi |
| 2 | Çatışan API client-lər | 7 yeni + 2 updated fayl |
| 3 | React Query hooks + komponent data binding | 8 hook + ~30 komponent update |
| 4 | Catch-all → ayrı page-lər | ~35 yeni page.tsx |
| 5 | UX: toast, skeleton, responsive, dark mode | ~10 fayl |
| 6 | Frontend Docker + compose | 3-4 fayl |
| 7 | Backend deploy hazırlığı | env, CORS, restart policy |
| 8 | End-to-end test | Yoxlama ssenarisi |
