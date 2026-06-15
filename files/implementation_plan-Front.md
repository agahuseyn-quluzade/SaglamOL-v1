# SaglamOL Frontend Architecture Plan

## 1. Backend Analiz Xülasəsi

### 1.1 Layihə Arxitekturası
SaglamOL çox şirkətli (multi-tenant) E-Health Insurance mikroservis platformasıdır. Platformada bir neçə sığorta şirkəti (`InsuranceCompany`) qoşulur və `insuranceCompanyId` ilə scope-lanmış policy, claim, payment əməliyyatları aparılır.

**Texnologiyalar:** Java 21, Spring Boot 3.3.6, PostgreSQL, Redis, Kafka, MinIO, API Gateway (port 8080)

### 1.2 Mövcud Service-lər və Port-lar

| Service | Port | Əsas məsuliyyət |
|---|---|---|
| API Gateway | 8080 | JWT validation, routing, CORS, rate limiting |
| IAM Service | 8081 | Auth, register, login, role, password management |
| User Profile Service | 8082 | Patient, Doctor, Agent, Hospital, InsuranceCompany profilleri |
| Policy Service | 8083 | InsuranceProduct, CoverageRule, Policy, ProviderContract, Eligibility |
| Claim Service | 8084 | Claim lifecycle, review, documents |
| Health Record Service | 8085 | Health records, treatments, MinIO document upload/download |
| AI Risk Service | 8086 | AI-based risk assessment |
| Fraud Detection Service | 8087 | Rule-based fraud scoring |
| Notification Service | 8088 | Template-based notifications (email/SMS/in-app) |
| Payment Service | 8089 | Premium payments, claim payouts, invoices |

### 1.3 User Role-ları

| Role | Təsvir |
|---|---|
| `ADMIN` | Platform super admin — hər yerə full access |
| `PATIENT` | Pasiyent — öz profili, policy, claim, health record, payment |
| `DOCTOR` | Həkim — öz profili, hospital scope, health records |
| `AGENT` | Sığorta agenti — öz company scope, policy satışı, claim review |
| `HOSPITAL_ADMIN` | Xəstəxana admini — öz hospitalı, staff, doctors, contracts |
| `HOSPITAL_STAFF` | Xəstəxana işçisi — öz hospital scope, claims |
| `INSURANCE_ADMIN` | Sığorta şirkəti admini — öz company, staff, products, policies |
| `INSURANCE_STAFF` | Sığorta şirkəti işçisi — öz company view |

### 1.4 API Endpoint Xəritəsi (26 Controller)

#### IAM Service (`/api/v1/iam`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/register` | Patient register | Public |
| POST | `/login` | Login (email/phone) | Public |
| POST | `/login/email` | Email login | Public |
| POST | `/login/phone` | Phone login | Public |
| POST | `/refresh` | Token refresh | Public |
| POST | `/logout` | Logout | Auth |
| POST | `/refresh-tokens/revoke` | Revoke token | Auth |
| GET | `/me` | Current user | Auth |
| GET | `/users` | List users | ADMIN |
| GET | `/users/{id}` | Get user | ADMIN |
| GET | `/users/search?q=` | Search users | ADMIN |
| PATCH | `/users/{id}/status` | Change status | ADMIN |
| POST | `/users/{id}/roles` | Assign role | ADMIN |
| DELETE | `/users/{id}/roles/{role}` | Remove role | ADMIN |
| POST | `/password/change` | Change password | Auth |
| POST | `/password/reset-request` | Request reset | Public |
| POST | `/password/reset-confirm` | Confirm reset | Public |

#### User Profile Service (`/api/v1/profiles`, `/api/v1/insurance-companies`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/profiles/patients` | Create patient profile | PATIENT |
| GET | `/profiles/patients/me` | My patient profile | PATIENT |
| GET | `/profiles/patients/{id}` | Get patient | Auth + ownership |
| PUT | `/profiles/patients/{id}` | Update patient | Auth + ownership |
| GET | `/profiles/patients/search` | Search patients | ADMIN, AGENT |
| POST | `/profiles/doctors` | Create doctor profile | DOCTOR |
| GET | `/profiles/doctors/me` | My doctor profile | DOCTOR |
| GET | `/profiles/doctors/{id}` | Get doctor | Auth + scope |
| PUT | `/profiles/doctors/{id}` | Update doctor | Auth + ownership |
| GET | `/profiles/doctors/search` | Search doctors | ADMIN, AGENT, HOSPITAL |
| POST | `/profiles/agents` | Create agent profile | AGENT |
| GET | `/profiles/agents/me` | My agent profile | AGENT |
| GET | `/profiles/agents/{id}` | Get agent | Auth + ownership |
| PUT | `/profiles/agents/{id}` | Update agent | Auth + ownership |
| GET | `/profiles/agents/search` | Search agents | ADMIN |
| PATCH | `/profiles/agents/{id}/insurance-company/{companyId}` | Link agent to company | ADMIN, INS_ADMIN |
| GET | `/profiles/agents/by-company/{companyId}` | Agents by company | INS_ADMIN, ADMIN |
| POST | `/profiles/hospitals` | Create hospital | ADMIN |
| GET | `/profiles/hospitals` | Search hospitals | Auth |
| GET | `/profiles/hospitals/{id}` | Get hospital | Auth |
| PUT | `/profiles/hospitals/{id}` | Update hospital | ADMIN, HOSP_ADMIN |
| PATCH | `/profiles/hospitals/{id}/status` | Change status | ADMIN |
| POST | `/profiles/hospitals/{id}/branches` | Create branch | ADMIN, HOSP_ADMIN |
| GET | `/profiles/hospitals/{id}/branches` | List branches | Auth |
| POST | `/profiles/hospitals/{id}/staff` | Create staff | ADMIN, HOSP_ADMIN |
| GET | `/profiles/hospitals/{id}/staff` | List staff | ADMIN, HOSP_ADMIN |
| POST | `/profiles/hospitals/{id}/doctors/{docId}` | Assign doctor | ADMIN, HOSP_ADMIN |
| GET | `/profiles/hospitals/{id}/doctors` | List doctors | Auth |
| POST | `/insurance-companies` | Create company | ADMIN |
| GET | `/insurance-companies` | List companies | Auth + scope |
| GET | `/insurance-companies/{id}` | Get company | Auth + scope |
| PUT | `/insurance-companies/{id}` | Update company | ADMIN, INS_ADMIN |
| PATCH | `/insurance-companies/{id}/status` | Change status | ADMIN |
| POST | `/insurance-companies/{id}/staff` | Create staff | ADMIN, INS_ADMIN |
| GET | `/insurance-companies/{id}/staff` | List staff | Auth + scope |
| GET | `/insurance-companies/{id}/staff/{staffId}` | Get staff | Auth + scope |
| PATCH | `/insurance-companies/{id}/staff/{staffId}/status` | Change staff status | ADMIN, INS_ADMIN |

#### Policy Service (`/api/v1/policies`, `/api/v1/insurance-products`, `/api/v1/provider-contracts`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/insurance-products` | Create product | INS_ADMIN, ADMIN |
| PUT | `/insurance-products/{id}` | Update product | INS_ADMIN, ADMIN |
| PATCH | `/insurance-products/{id}/status` | Change status | INS_ADMIN, ADMIN |
| GET | `/insurance-products/{id}` | Get product | Auth |
| GET | `/insurance-products` | Search products | Auth |
| POST | `/insurance-products/{id}/coverage-rules` | Add rule | INS_ADMIN, ADMIN |
| PUT | `/insurance-products/{id}/coverage-rules/{ruleId}` | Update rule | INS_ADMIN, ADMIN |
| PATCH | `/insurance-products/{id}/coverage-rules/{ruleId}/status` | Change status | INS_ADMIN, ADMIN |
| GET | `/insurance-products/{id}/coverage-rules` | List rules | Auth |
| POST | `/policies` | Issue policy | AGENT, ADMIN |
| GET | `/policies/{id}` | Get policy | Auth + scope |
| GET | `/policies/me` | My policies | PATIENT |
| GET | `/policies` | Search policies | Auth + scope |
| POST | `/policies/eligibility-check` | Check eligibility | Auth |
| PATCH | `/policies/{id}/cancel` | Cancel policy | Auth + scope |
| PATCH | `/policies/{id}/suspend` | Suspend policy | Auth + scope |
| POST | `/provider-contracts` | Create contract | INS_ADMIN, ADMIN |
| GET | `/provider-contracts/{id}` | Get contract | Auth + scope |
| GET | `/provider-contracts/by-company/{id}` | By company | INS_ADMIN, ADMIN |
| GET | `/provider-contracts/by-hospital/{id}` | By hospital | HOSP_ADMIN |
| PATCH | `/provider-contracts/{id}/terminate` | Terminate | INS_ADMIN, ADMIN |

#### Claim Service (`/api/v1/claims`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/claims` | Create draft claim | PATIENT, HOSP_STAFF |
| POST | `/claims/{id}/items` | Add item | Auth + ownership |
| POST | `/claims/{id}/documents` | Attach document | Auth + ownership |
| POST | `/claims/{id}/submit` | Submit claim | Auth + ownership |
| GET | `/claims/{id}` | Get claim | Auth + scope |
| GET | `/claims/my` | My claims | PATIENT |
| GET | `/claims` | Search claims | Auth + scope |
| POST | `/claims/{id}/review/start` | Start review | AGENT, INS_ADMIN |
| POST | `/claims/{id}/review/more-documents` | Request docs | AGENT, INS_ADMIN |
| POST | `/claims/{id}/review/approve` | Approve | AGENT, INS_ADMIN |
| POST | `/claims/{id}/review/reject` | Reject | AGENT, INS_ADMIN |
| PUT | `/claims/{id}/review/cancel` | Cancel | Auth + ownership |

#### Payment Service (`/api/v1/payments`, `/api/v1/invoices`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/payments/policy-premium` | Create premium payment | PATIENT, AGENT |
| POST | `/payments/claim-payout` | Create payout | INS_ADMIN, ADMIN |
| POST | `/payments/{id}/complete-mock` | Complete mock | Auth |
| POST | `/payments/{id}/fail-mock` | Fail mock | Auth |
| POST | `/payments/{id}/refund-mock` | Refund mock | Auth |
| GET | `/payments/{id}` | Get payment | Auth + scope |
| GET | `/payments/my` | My payments | PATIENT |
| GET | `/payments/by-policy` | By policy | Auth + scope |
| GET | `/payments/by-claim` | By claim | Auth + scope |
| GET | `/payments/by-company` | By company | INS_ADMIN, ADMIN |
| GET | `/payments/by-hospital` | By hospital | HOSP_ADMIN |
| POST | `/invoices` | Create invoice | INS_ADMIN, ADMIN |
| GET | `/invoices/{id}` | Get invoice | Auth + scope |
| GET | `/invoices/by-company` | By company | INS_ADMIN, ADMIN |
| GET | `/invoices/by-hospital` | By hospital | HOSP_ADMIN |
| POST | `/invoices/{id}/issue` | Issue invoice | INS_ADMIN |
| POST | `/invoices/{id}/mark-paid` | Mark paid | INS_ADMIN |
| POST | `/invoices/{id}/cancel` | Cancel | INS_ADMIN |

#### Health Record Service (`/api/v1/health-records`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/health-records` | Create record | DOCTOR, HOSP_STAFF |
| GET | `/health-records/my` | My records | PATIENT |
| GET | `/health-records/{id}` | Get record + access log | Auth + scope |
| GET | `/health-records/by-claim/{claimId}` | By claim | Auth + scope |
| POST | `/health-records/{id}/treatments` | Add treatment | DOCTOR |
| PATCH | `/health-records/{id}/archive` | Archive | DOCTOR, ADMIN |
| POST | `/health-records/{id}/documents/uploads` | Initiate upload | DOCTOR, HOSP_STAFF |
| PUT | `/health-records/{id}/documents/{docId}/confirm` | Confirm upload | Auth |
| GET | `/health-records/{id}/documents/{docId}` | Get document | Auth + scope |
| DELETE | `/health-records/{id}/documents/{docId}` | Delete document | Auth + scope |

#### Fraud Detection (`/fraud`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/fraud/claims/{id}/check` | Run fraud check | INS_ADMIN, AGENT |
| GET | `/fraud/claims/{id}` | Get by claim | Auth + scope |
| GET | `/fraud/assessments/{id}` | Get by ID | Auth + scope |
| GET | `/fraud/companies/{id}/summary` | Company summary | INS_ADMIN, ADMIN |
| GET | `/fraud/hospitals/{id}/summary` | Hospital summary | HOSP_ADMIN, ADMIN |

#### AI Risk (`/ai-risk`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/ai-risk/claims/{id}/assess` | Run AI assessment | INS_ADMIN, AGENT |
| GET | `/ai-risk/claims/{id}` | Get by claim | Auth + scope |
| GET | `/ai-risk/assessments/{id}` | Get by ID | Auth + scope |
| GET | `/ai-risk/companies/{id}/summary` | Company summary | INS_ADMIN, ADMIN |

#### Notification Service (`/notifications`)
| Method | Endpoint | Təsvir | Auth |
|---|---|---|---|
| POST | `/notifications/send` | Send notification | ADMIN |
| GET | `/notifications/{id}` | Get notification | Auth + ownership |
| GET | `/notifications/my` | My notifications | Auth |
| GET | `/notifications/by-user/{userId}` | By user | ADMIN |
| GET | `/notifications/by-company/{companyId}` | By company | INS_ADMIN, ADMIN |
| POST | `/notifications/templates` | Create template | ADMIN |
| GET | `/notifications/templates` | List templates | ADMIN |
| PUT | `/notifications/templates/{id}` | Update template | ADMIN |
| PATCH | `/notifications/templates/{id}/status` | Change status | ADMIN |

---

## 2. Frontend Tech Stack Tövsiyəsi

| Kateqoriya | Texnologiya | Səbəb |
|---|---|---|
| Framework | **React 18 + Vite 6** | Sürətli dev, HMR, TypeScript built-in |
| Dil | **TypeScript (strict)** | Type safety, DTO uyğunluğu |
| Styling | **Tailwind CSS v4** | Utility-first, dark mode, responsive |
| State (Server) | **TanStack React Query v5** | Cache, pagination, optimistic updates |
| State (Client) | **Zustand** | Auth, theme, sidebar — lightweight |
| Routing | **React Router v7** | Nested routes, lazy loading, guards |
| Forms | **React Hook Form + Zod** | Performant forms, schema validation |
| HTTP Client | **Axios** | Interceptors, refresh token, error handling |
| Table | **TanStack Table v8** | Server-side pagination, sort, filter |
| UI Components | **Radix UI + Custom** | Accessible primitives, styled üstündən |
| Icons | **Lucide React** | Modern, tree-shakable |
| Notifications | **Sonner** | Toast notification system |
| Charts | **Recharts** | Dashboard charts, analytics |
| Date | **date-fns** | Lightweight date formatting |

---

## 3. Folder Structure

```
saglamol-frontend/
├── public/
│   └── favicon.svg
├── src/
│   ├── app/                           # App-level setup
│   │   ├── App.tsx                    # Root component
│   │   ├── Router.tsx                 # All routes
│   │   └── Providers.tsx              # QueryClient, ThemeProvider, etc.
│   │
│   ├── assets/                        # Static assets
│   │   ├── images/
│   │   └── fonts/
│   │
│   ├── config/                        # App config
│   │   ├── api.config.ts              # Base URL, timeouts
│   │   ├── routes.config.ts           # Route path constants
│   │   └── roles.config.ts            # Role constants & permission maps
│   │
│   ├── lib/                           # Core utilities
│   │   ├── axios.ts                   # Axios instance + interceptors
│   │   ├── query-client.ts            # React Query client
│   │   └── utils.ts                   # cn(), formatDate, etc.
│   │
│   ├── stores/                        # Zustand stores
│   │   ├── auth.store.ts              # Auth state, tokens
│   │   ├── theme.store.ts             # Dark/light mode
│   │   └── sidebar.store.ts           # Sidebar open/collapsed
│   │
│   ├── hooks/                         # Shared hooks
│   │   ├── use-auth.ts
│   │   ├── use-permissions.ts
│   │   ├── use-debounce.ts
│   │   └── use-pagination.ts
│   │
│   ├── types/                         # Global TypeScript types
│   │   ├── api.types.ts               # PageResponse, ErrorResponse
│   │   ├── auth.types.ts              # TokenResponse, MeResponse
│   │   ├── profile.types.ts           # Patient, Doctor, Agent, Hospital
│   │   ├── insurance.types.ts         # InsuranceCompany, Staff
│   │   ├── policy.types.ts            # Product, CoverageRule, Policy
│   │   ├── claim.types.ts             # Claim, ClaimItem, ClaimReview
│   │   ├── payment.types.ts           # Payment, Invoice
│   │   ├── health-record.types.ts     # HealthRecord, MedicalDocument
│   │   ├── fraud.types.ts             # FraudAssessment, FraudSignal
│   │   ├── ai-risk.types.ts           # AiRiskAssessment
│   │   └── notification.types.ts      # Notification, Template
│   │
│   ├── services/                      # API service layer
│   │   ├── auth.service.ts
│   │   ├── user.service.ts
│   │   ├── profile.service.ts
│   │   ├── insurance-company.service.ts
│   │   ├── hospital.service.ts
│   │   ├── policy.service.ts
│   │   ├── product.service.ts
│   │   ├── coverage-rule.service.ts
│   │   ├── provider-contract.service.ts
│   │   ├── claim.service.ts
│   │   ├── claim-review.service.ts
│   │   ├── payment.service.ts
│   │   ├── invoice.service.ts
│   │   ├── health-record.service.ts
│   │   ├── medical-document.service.ts
│   │   ├── fraud.service.ts
│   │   ├── ai-risk.service.ts
│   │   └── notification.service.ts
│   │
│   ├── queries/                       # React Query hooks
│   │   ├── auth.queries.ts
│   │   ├── profile.queries.ts
│   │   ├── insurance.queries.ts
│   │   ├── hospital.queries.ts
│   │   ├── policy.queries.ts
│   │   ├── product.queries.ts
│   │   ├── claim.queries.ts
│   │   ├── payment.queries.ts
│   │   ├── health-record.queries.ts
│   │   ├── fraud.queries.ts
│   │   ├── ai-risk.queries.ts
│   │   └── notification.queries.ts
│   │
│   ├── components/                    # Reusable UI components
│   │   ├── ui/                        # Base primitives
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Textarea.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Dialog.tsx             # Modal
│   │   │   ├── Drawer.tsx
│   │   │   ├── DropdownMenu.tsx
│   │   │   ├── Tabs.tsx
│   │   │   ├── Avatar.tsx
│   │   │   ├── Tooltip.tsx
│   │   │   ├── Skeleton.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── Switch.tsx
│   │   │   └── Separator.tsx
│   │   │
│   │   ├── data/                      # Data display
│   │   │   ├── DataTable.tsx          # Server-side paginated table
│   │   │   ├── StatCard.tsx           # Dashboard stat card
│   │   │   ├── StatusBadge.tsx        # Color-coded status
│   │   │   ├── RoleBadge.tsx          # Role badge component
│   │   │   ├── AmountDisplay.tsx      # AZN formatted money
│   │   │   ├── DateDisplay.tsx        # Formatted dates
│   │   │   └── EmptyState.tsx         # No data placeholder
│   │   │
│   │   ├── form/                      # Form components
│   │   │   ├── FormField.tsx
│   │   │   ├── FormInput.tsx
│   │   │   ├── FormSelect.tsx
│   │   │   ├── FormTextarea.tsx
│   │   │   ├── FormDatePicker.tsx
│   │   │   ├── FormMoneyInput.tsx
│   │   │   └── FormSwitch.tsx
│   │   │
│   │   ├── feedback/                  # Feedback components
│   │   │   ├── ErrorBoundary.tsx
│   │   │   ├── ErrorState.tsx
│   │   │   ├── LoadingSkeleton.tsx
│   │   │   ├── PageLoader.tsx
│   │   │   └── ConfirmDialog.tsx
│   │   │
│   │   └── layout/                    # Layout components
│   │       ├── Sidebar.tsx
│   │       ├── SidebarItem.tsx
│   │       ├── Topbar.tsx
│   │       ├── UserMenu.tsx
│   │       ├── ThemeToggle.tsx
│   │       ├── NotificationBell.tsx
│   │       ├── Breadcrumb.tsx
│   │       └── PageHeader.tsx
│   │
│   ├── guards/                        # Route guards
│   │   ├── AuthGuard.tsx              # Redirect if not auth
│   │   ├── GuestGuard.tsx             # Redirect if auth
│   │   └── RoleGuard.tsx              # Check role permissions
│   │
│   ├── layouts/                       # Page layouts
│   │   ├── DashboardLayout.tsx        # Sidebar + Topbar + Content
│   │   ├── AuthLayout.tsx             # Login/Register pages
│   │   └── MinimalLayout.tsx          # Error pages
│   │
│   ├── pages/                         # Page components
│   │   ├── auth/
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   ├── ForgotPasswordPage.tsx
│   │   │   └── ResetPasswordPage.tsx
│   │   │
│   │   ├── dashboard/
│   │   │   ├── AdminDashboard.tsx
│   │   │   ├── PatientDashboard.tsx
│   │   │   ├── DoctorDashboard.tsx
│   │   │   ├── AgentDashboard.tsx
│   │   │   ├── InsuranceAdminDashboard.tsx
│   │   │   ├── HospitalAdminDashboard.tsx
│   │   │   └── DashboardRouter.tsx    # Role-based redirect
│   │   │
│   │   ├── users/                     # ADMIN only
│   │   │   ├── UsersListPage.tsx
│   │   │   └── UserDetailPage.tsx
│   │   │
│   │   ├── profiles/
│   │   │   ├── MyProfilePage.tsx      # Patient/Doctor/Agent profili
│   │   │   ├── PatientListPage.tsx
│   │   │   ├── PatientDetailPage.tsx
│   │   │   ├── DoctorListPage.tsx
│   │   │   ├── DoctorDetailPage.tsx
│   │   │   ├── AgentListPage.tsx
│   │   │   └── AgentDetailPage.tsx
│   │   │
│   │   ├── insurance-companies/
│   │   │   ├── CompanyListPage.tsx
│   │   │   ├── CompanyDetailPage.tsx
│   │   │   ├── CompanyCreatePage.tsx
│   │   │   ├── CompanyStaffPage.tsx
│   │   │   └── CompanyAgentsPage.tsx
│   │   │
│   │   ├── hospitals/
│   │   │   ├── HospitalListPage.tsx
│   │   │   ├── HospitalDetailPage.tsx
│   │   │   ├── HospitalCreatePage.tsx
│   │   │   ├── HospitalBranchesPage.tsx
│   │   │   ├── HospitalStaffPage.tsx
│   │   │   └── HospitalDoctorsPage.tsx
│   │   │
│   │   ├── products/
│   │   │   ├── ProductListPage.tsx
│   │   │   ├── ProductDetailPage.tsx
│   │   │   ├── ProductCreatePage.tsx
│   │   │   └── CoverageRulesPage.tsx
│   │   │
│   │   ├── policies/
│   │   │   ├── PolicyListPage.tsx
│   │   │   ├── PolicyDetailPage.tsx
│   │   │   ├── PolicyIssuePage.tsx
│   │   │   ├── MyPoliciesPage.tsx
│   │   │   └── EligibilityCheckPage.tsx
│   │   │
│   │   ├── claims/
│   │   │   ├── ClaimListPage.tsx
│   │   │   ├── ClaimDetailPage.tsx
│   │   │   ├── ClaimCreatePage.tsx
│   │   │   ├── ClaimReviewPage.tsx
│   │   │   └── MyClaimsPage.tsx
│   │   │
│   │   ├── payments/
│   │   │   ├── PaymentListPage.tsx
│   │   │   ├── PaymentDetailPage.tsx
│   │   │   ├── PaymentCreatePage.tsx
│   │   │   ├── MyPaymentsPage.tsx
│   │   │   └── InvoiceListPage.tsx
│   │   │
│   │   ├── health-records/
│   │   │   ├── HealthRecordListPage.tsx
│   │   │   ├── HealthRecordDetailPage.tsx
│   │   │   ├── HealthRecordCreatePage.tsx
│   │   │   ├── MyHealthRecordsPage.tsx
│   │   │   └── DocumentUploadPage.tsx
│   │   │
│   │   ├── fraud/
│   │   │   ├── FraudDashboardPage.tsx
│   │   │   ├── FraudAssessmentDetailPage.tsx
│   │   │   └── FraudCompanySummaryPage.tsx
│   │   │
│   │   ├── ai-risk/
│   │   │   ├── AiRiskDashboardPage.tsx
│   │   │   └── AiRiskAssessmentDetailPage.tsx
│   │   │
│   │   ├── notifications/
│   │   │   ├── NotificationsPage.tsx
│   │   │   └── NotificationTemplatesPage.tsx  # ADMIN only
│   │   │
│   │   ├── contracts/
│   │   │   ├── ContractListPage.tsx
│   │   │   ├── ContractDetailPage.tsx
│   │   │   └── ContractCreatePage.tsx
│   │   │
│   │   ├── settings/
│   │   │   ├── ChangePasswordPage.tsx
│   │   │   └── PreferencesPage.tsx
│   │   │
│   │   └── errors/
│   │       ├── NotFoundPage.tsx
│   │       ├── ForbiddenPage.tsx
│   │       └── ServerErrorPage.tsx
│   │
│   ├── index.css                      # Tailwind base + custom tokens
│   └── main.tsx                       # Entry point
│
├── .env                               # VITE_API_BASE_URL
├── .env.example
├── index.html
├── package.json
├── tailwind.config.ts
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## 4. Routing Xəritəsi

```typescript
// src/config/routes.config.ts
export const ROUTES = {
  // Auth
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',
  RESET_PASSWORD: '/reset-password',
  
  // Dashboard
  DASHBOARD: '/',
  
  // Users (ADMIN)
  USERS: '/users',
  USER_DETAIL: '/users/:id',
  
  // Profiles
  MY_PROFILE: '/profile',
  PATIENTS: '/patients',
  PATIENT_DETAIL: '/patients/:id',
  DOCTORS: '/doctors',
  DOCTOR_DETAIL: '/doctors/:id',
  AGENTS: '/agents',
  AGENT_DETAIL: '/agents/:id',
  
  // Insurance Companies
  COMPANIES: '/insurance-companies',
  COMPANY_DETAIL: '/insurance-companies/:id',
  COMPANY_CREATE: '/insurance-companies/new',
  COMPANY_STAFF: '/insurance-companies/:id/staff',
  COMPANY_AGENTS: '/insurance-companies/:id/agents',
  
  // Hospitals
  HOSPITALS: '/hospitals',
  HOSPITAL_DETAIL: '/hospitals/:id',
  HOSPITAL_CREATE: '/hospitals/new',
  HOSPITAL_BRANCHES: '/hospitals/:id/branches',
  HOSPITAL_STAFF: '/hospitals/:id/staff',
  HOSPITAL_DOCTORS: '/hospitals/:id/doctors',
  
  // Products
  PRODUCTS: '/products',
  PRODUCT_DETAIL: '/products/:id',
  PRODUCT_CREATE: '/products/new',
  COVERAGE_RULES: '/products/:id/coverage-rules',
  
  // Policies
  POLICIES: '/policies',
  POLICY_DETAIL: '/policies/:id',
  POLICY_ISSUE: '/policies/new',
  MY_POLICIES: '/my-policies',
  ELIGIBILITY_CHECK: '/eligibility-check',
  
  // Claims
  CLAIMS: '/claims',
  CLAIM_DETAIL: '/claims/:id',
  CLAIM_CREATE: '/claims/new',
  CLAIM_REVIEW: '/claims/:id/review',
  MY_CLAIMS: '/my-claims',
  
  // Payments
  PAYMENTS: '/payments',
  PAYMENT_DETAIL: '/payments/:id',
  MY_PAYMENTS: '/my-payments',
  INVOICES: '/invoices',
  
  // Health Records
  HEALTH_RECORDS: '/health-records',
  HEALTH_RECORD_DETAIL: '/health-records/:id',
  HEALTH_RECORD_CREATE: '/health-records/new',
  MY_HEALTH_RECORDS: '/my-health-records',
  
  // Fraud & AI
  FRAUD_DASHBOARD: '/fraud',
  FRAUD_DETAIL: '/fraud/:id',
  AI_RISK_DASHBOARD: '/ai-risk',
  AI_RISK_DETAIL: '/ai-risk/:id',
  
  // Contracts
  CONTRACTS: '/provider-contracts',
  CONTRACT_DETAIL: '/provider-contracts/:id',
  CONTRACT_CREATE: '/provider-contracts/new',
  
  // Notifications
  NOTIFICATIONS: '/notifications',
  NOTIFICATION_TEMPLATES: '/notifications/templates',
  
  // Settings
  CHANGE_PASSWORD: '/settings/password',
  PREFERENCES: '/settings/preferences',
  
  // Errors
  NOT_FOUND: '/404',
  FORBIDDEN: '/403',
  SERVER_ERROR: '/500',
} as const;
```

---

## 5. Role-Based Access Matrix (Frontend Səhifələri)

| Səhifə | ADMIN | INS_ADMIN | INS_STAFF | AGENT | PATIENT | DOCTOR | HOSP_ADMIN | HOSP_STAFF |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Admin Dashboard | ✅ | | | | | | | |
| Users Management | ✅ | | | | | | | |
| Insurance Companies | ✅ | ✅ (own) | ✅ (own) | ✅ (own) | | | | |
| Company Staff | ✅ | ✅ (own) | | | | | | |
| Insurance Products | ✅ | ✅ (own) | ✅ (own) | ✅ (ACTIVE) | ✅ (ACTIVE) | | | |
| Coverage Rules | ✅ | ✅ (own) | | | | | | |
| Hospitals | ✅ | | | | | | ✅ (own) | ✅ (own) |
| Hospital Staff/Branches | ✅ | | | | | | ✅ (own) | |
| Policies (All) | ✅ | ✅ (own) | ✅ (own) | ✅ (own) | | | | |
| My Policies | | | | | ✅ | | | |
| Policy Issue | ✅ | ✅ | | ✅ | | | | |
| Eligibility Check | ✅ | ✅ | | ✅ | ✅ | | ✅ | |
| Claims (All) | ✅ | ✅ (own) | | ✅ (own) | | | ✅ (own) | ✅ (own) |
| My Claims | | | | | ✅ | | | |
| Claim Create | | | | | ✅ | | | ✅ |
| Claim Review | ✅ | ✅ (own) | | ✅ (own) | | | | |
| Payments (All) | ✅ | ✅ (own) | | ✅ (own) | | | ✅ (own) | |
| My Payments | | | | | ✅ | | | |
| Health Records | ✅ | | | | | ✅ (scope) | ✅ (own) | ✅ (own) |
| My Health Records | | | | | ✅ | | | |
| Fraud Dashboard | ✅ | ✅ (own) | | ✅ (own) | | | ✅ (own) | |
| AI Risk Dashboard | ✅ | ✅ (own) | | ✅ (own) | | | | |
| Notifications | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Notification Templates | ✅ | | | | | | | |
| Provider Contracts | ✅ | ✅ (own) | | | | | ✅ (own) | |
| Invoices | ✅ | ✅ (own) | | | | | ✅ (own) | |
| Profile (own) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Change Password | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## 6. Dashboard Layout Planı

### 6.1 Sidebar Navigation (Role-based)

**ADMIN:**
- Dashboard
- Users
- Insurance Companies
- Hospitals
- Products
- Policies
- Claims
- Payments & Invoices
- Health Records
- Fraud Detection
- AI Risk Analysis
- Provider Contracts
- Notifications (+ Templates)
- Settings

**INSURANCE_ADMIN:**
- Dashboard
- My Company
- Staff Management
- Agents
- Products & Coverage
- Policies
- Claims & Review
- Payments & Invoices
- Fraud Reports
- AI Risk Reports
- Provider Contracts
- Notifications
- Settings

**AGENT:**
- Dashboard
- My Company
- Products
- Issue Policy
- Policies
- Claims & Review
- Notifications
- Settings

**PATIENT:**
- Dashboard
- My Profile
- My Policies
- My Claims
- My Payments
- My Health Records
- Notifications
- Settings

**DOCTOR:**
- Dashboard
- My Profile
- Health Records
- Notifications
- Settings

**HOSPITAL_ADMIN:**
- Dashboard
- My Hospital
- Branches
- Staff
- Doctors
- Claims
- Payments & Invoices
- Health Records
- Provider Contracts
- Fraud Reports
- Notifications
- Settings

### 6.2 Dashboard Stat Cards (Role-based)

**ADMIN Dashboard:** Total Users, Active Companies, Active Policies, Pending Claims, Revenue (AZN), Fraud Alerts, System Health
**PATIENT Dashboard:** Active Policies, Pending Claims, Total Payments, Health Records
**INSURANCE_ADMIN Dashboard:** Active Products, Active Policies, Pending Claims, Revenue, Fraud Score Avg, Provider Contracts
**AGENT Dashboard:** Policies Sold, Pending Reviews, Active Clients, Commission Stats
**HOSPITAL_ADMIN Dashboard:** Active Contracts, Pending Claims, Staff Count, Payout Stats

---

## 7. Kod Nümunələri

### 7.1 Axios Client

```typescript
// src/lib/axios.ts
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/auth.store';
import { API_CONFIG } from '@/config/api.config';

const apiClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — JWT token əlavə et
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor — 401 üçün auto refresh
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { refreshToken } = useAuthStore.getState();
        if (!refreshToken) throw new Error('No refresh token');

        const { data } = await axios.post(`${API_CONFIG.BASE_URL}/api/v1/iam/refresh`, {
          refreshToken,
        });

        useAuthStore.getState().setTokens(data.accessToken, data.refreshToken);
        processQueue(null, data.accessToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError as AxiosError, null);
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

### 7.2 Auth Service

```typescript
// src/services/auth.service.ts
import apiClient from '@/lib/axios';
import type {
  LoginRequest, RegisterRequest, TokenResponse,
  MeResponse, PasswordResetRequest, PasswordResetConfirmRequest,
  ChangePasswordRequest, OperationResponse
} from '@/types/auth.types';

const AUTH_BASE = '/api/v1/iam';

export const authService = {
  register: (data: RegisterRequest) =>
    apiClient.post<TokenResponse>(`${AUTH_BASE}/register`, data).then(r => r.data),

  login: (data: LoginRequest) =>
    apiClient.post<TokenResponse>(`${AUTH_BASE}/login`, data).then(r => r.data),

  loginWithEmail: (email: string, password: string) =>
    apiClient.post<TokenResponse>(`${AUTH_BASE}/login/email`, { email, password }).then(r => r.data),

  loginWithPhone: (phoneNumber: string, password: string) =>
    apiClient.post<TokenResponse>(`${AUTH_BASE}/login/phone`, { phoneNumber, password }).then(r => r.data),

  refresh: (refreshToken: string) =>
    apiClient.post<TokenResponse>(`${AUTH_BASE}/refresh`, { refreshToken }).then(r => r.data),

  logout: (refreshToken: string) =>
    apiClient.post<OperationResponse>(`${AUTH_BASE}/logout`, { refreshToken }).then(r => r.data),

  me: () =>
    apiClient.get<MeResponse>(`${AUTH_BASE}/me`).then(r => r.data),

  changePassword: (data: ChangePasswordRequest) =>
    apiClient.post<OperationResponse>(`${AUTH_BASE}/password/change`, data).then(r => r.data),

  requestPasswordReset: (data: PasswordResetRequest) =>
    apiClient.post(`${AUTH_BASE}/password/reset-request`, data).then(r => r.data),

  confirmPasswordReset: (data: PasswordResetConfirmRequest) =>
    apiClient.post<OperationResponse>(`${AUTH_BASE}/password/reset-confirm`, data).then(r => r.data),
};
```

### 7.3 Auth Store (Zustand)

```typescript
// src/stores/auth.store.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { MeResponse } from '@/types/auth.types';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: MeResponse | null;
  isAuthenticated: boolean;

  setTokens: (access: string, refresh: string) => void;
  setUser: (user: MeResponse) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (...roles: string[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, isAuthenticated: true }),

      setUser: (user) => set({ user }),

      logout: () =>
        set({ accessToken: null, refreshToken: null, user: null, isAuthenticated: false }),

      hasRole: (role) => {
        const { user } = get();
        return user?.roles?.includes(role) ?? false;
      },

      hasAnyRole: (...roles) => {
        const { user } = get();
        return roles.some((role) => user?.roles?.includes(role)) ?? false;
      },
    }),
    { name: 'saglamol-auth' }
  )
);
```

### 7.4 Protected Route Guard

```tsx
// src/guards/AuthGuard.tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth.store';
import { ROUTES } from '@/config/routes.config';

export function AuthGuard() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <Outlet />;
}
```

```tsx
// src/guards/RoleGuard.tsx
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth.store';
import { ROUTES } from '@/config/routes.config';

interface RoleGuardProps {
  allowedRoles: string[];
}

export function RoleGuard({ allowedRoles }: RoleGuardProps) {
  const hasAnyRole = useAuthStore((s) => s.hasAnyRole);

  if (!hasAnyRole(...allowedRoles)) {
    return <Navigate to={ROUTES.FORBIDDEN} replace />;
  }

  return <Outlet />;
}
```

### 7.5 Dashboard Layout

```tsx
// src/layouts/DashboardLayout.tsx
import { Outlet } from 'react-router-dom';
import { Sidebar } from '@/components/layout/Sidebar';
import { Topbar } from '@/components/layout/Topbar';
import { useSidebarStore } from '@/stores/sidebar.store';
import { cn } from '@/lib/utils';

export function DashboardLayout() {
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);

  return (
    <div className="flex h-screen bg-gray-50 dark:bg-gray-900">
      <Sidebar />
      <div
        className={cn(
          'flex flex-1 flex-col transition-all duration-300',
          isCollapsed ? 'ml-16' : 'ml-64'
        )}
      >
        <Topbar />
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
```

### 7.6 Sidebar (Role-based)

```tsx
// src/components/layout/Sidebar.tsx (sadələşdirilmiş)
import { useAuthStore } from '@/stores/auth.store';
import { useSidebarStore } from '@/stores/sidebar.store';
import { ROLE_NAVIGATION } from '@/config/navigation.config';
import { SidebarItem } from './SidebarItem';
import { cn } from '@/lib/utils';

export function Sidebar() {
  const user = useAuthStore((s) => s.user);
  const isCollapsed = useSidebarStore((s) => s.isCollapsed);
  const toggle = useSidebarStore((s) => s.toggle);

  // İlk uyğun role üçün navigasiya al
  const primaryRole = user?.roles?.[0] ?? 'PATIENT';
  const navItems = ROLE_NAVIGATION[primaryRole] ?? ROLE_NAVIGATION.PATIENT;

  return (
    <aside
      className={cn(
        'fixed left-0 top-0 z-40 h-screen border-r border-gray-200 bg-white transition-all duration-300 dark:border-gray-700 dark:bg-gray-800',
        isCollapsed ? 'w-16' : 'w-64'
      )}
    >
      {/* Logo */}
      <div className="flex h-16 items-center justify-between px-4">
        {!isCollapsed && (
          <span className="text-xl font-bold text-emerald-600">SağlamOL</span>
        )}
        <button onClick={toggle} className="rounded-lg p-1.5 hover:bg-gray-100 dark:hover:bg-gray-700">
          {/* Menu icon */}
        </button>
      </div>

      {/* Navigation */}
      <nav className="mt-4 space-y-1 px-2">
        {navItems.map((item) => (
          <SidebarItem key={item.path} item={item} isCollapsed={isCollapsed} />
        ))}
      </nav>
    </aside>
  );
}
```

### 7.7 React Query Hook Nümunəsi

```typescript
// src/queries/policy.queries.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { policyService } from '@/services/policy.service';
import type { IssuePolicyRequest, PolicyStatus } from '@/types/policy.types';
import type { PageParams } from '@/types/api.types';
import { toast } from 'sonner';

export const policyKeys = {
  all: ['policies'] as const,
  lists: () => [...policyKeys.all, 'list'] as const,
  list: (filters: Record<string, unknown>) => [...policyKeys.lists(), filters] as const,
  details: () => [...policyKeys.all, 'detail'] as const,
  detail: (id: string) => [...policyKeys.details(), id] as const,
  my: () => [...policyKeys.all, 'my'] as const,
};

export function usePolicies(params: {
  companyId?: string;
  patientProfileId?: string;
  status?: PolicyStatus;
  page?: number;
  size?: number;
}) {
  return useQuery({
    queryKey: policyKeys.list(params),
    queryFn: () => policyService.searchPolicies(params),
  });
}

export function usePolicy(id: string) {
  return useQuery({
    queryKey: policyKeys.detail(id),
    queryFn: () => policyService.getPolicy(id),
    enabled: !!id,
  });
}

export function useMyPolicies() {
  return useQuery({
    queryKey: policyKeys.my(),
    queryFn: () => policyService.getMyPolicies(),
  });
}

export function useIssuePolicy() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: IssuePolicyRequest) => policyService.issuePolicy(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: policyKeys.all });
      toast.success('Policy uğurla yaradıldı');
    },
    onError: () => {
      toast.error('Policy yaradılarkən xəta baş verdi');
    },
  });
}
```

### 7.8 Form Nümunəsi (Policy Issue)

```tsx
// src/pages/policies/PolicyIssuePage.tsx (sadələşdirilmiş)
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useIssuePolicy } from '@/queries/policy.queries';
import { useProducts } from '@/queries/product.queries';
import { FormInput, FormSelect } from '@/components/form';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { PageHeader } from '@/components/layout/PageHeader';

const issuePolicySchema = z.object({
  productId: z.string().uuid('Product seçin'),
  patientProfileId: z.string().uuid('Pasiyent seçin'),
  agentProfileId: z.string().uuid().optional(),
  startDate: z.string().min(1, 'Başlanğıc tarix tələb olunur'),
  endDate: z.string().min(1, 'Bitmə tarixi tələb olunur'),
});

type IssuePolicyFormData = z.infer<typeof issuePolicySchema>;

export function PolicyIssuePage() {
  const issuePolicy = useIssuePolicy();
  const { data: products } = useProducts({ status: 'ACTIVE' });

  const form = useForm<IssuePolicyFormData>({
    resolver: zodResolver(issuePolicySchema),
    defaultValues: { productId: '', patientProfileId: '', startDate: '', endDate: '' },
  });

  const onSubmit = (data: IssuePolicyFormData) => {
    issuePolicy.mutate(data);
  };

  return (
    <div>
      <PageHeader title="Yeni Policy Yarat" subtitle="Sığorta polisi yarat" />
      <Card className="max-w-2xl">
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 p-6">
          <FormSelect
            label="Sığorta Məhsulu"
            options={products?.content?.map(p => ({ value: p.id, label: p.name })) ?? []}
            {...form.register('productId')}
            error={form.formState.errors.productId?.message}
          />
          <FormInput
            label="Pasiyent Profile ID"
            {...form.register('patientProfileId')}
            error={form.formState.errors.patientProfileId?.message}
          />
          <div className="grid grid-cols-2 gap-4">
            <FormInput label="Başlanğıc" type="date" {...form.register('startDate')} />
            <FormInput label="Bitmə" type="date" {...form.register('endDate')} />
          </div>
          <Button type="submit" loading={issuePolicy.isPending}>
            Policy Yarat
          </Button>
        </form>
      </Card>
    </div>
  );
}
```

### 7.9 DataTable Nümunəsi

```tsx
// src/pages/claims/ClaimListPage.tsx (sadələşdirilmiş)
import { useState } from 'react';
import { useClaims } from '@/queries/claim.queries';
import { DataTable } from '@/components/data/DataTable';
import { StatusBadge } from '@/components/data/StatusBadge';
import { AmountDisplay } from '@/components/data/AmountDisplay';
import { DateDisplay } from '@/components/data/DateDisplay';
import { PageHeader } from '@/components/layout/PageHeader';
import type { ColumnDef } from '@tanstack/react-table';
import type { ClaimSummary } from '@/types/claim.types';

const columns: ColumnDef<ClaimSummary>[] = [
  { accessorKey: 'claimNumber', header: 'Claim №' },
  { accessorKey: 'serviceType', header: 'Xidmət növü' },
  {
    accessorKey: 'claimAmount',
    header: 'Məbləğ',
    cell: ({ getValue }) => <AmountDisplay amount={getValue() as number} />,
  },
  {
    accessorKey: 'status',
    header: 'Status',
    cell: ({ getValue }) => <StatusBadge status={getValue() as string} />,
  },
  {
    accessorKey: 'createdAt',
    header: 'Yaradılma tarixi',
    cell: ({ getValue }) => <DateDisplay date={getValue() as string} />,
  },
];

export function ClaimListPage() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({});
  const { data, isLoading } = useClaims({ ...filters, page, size: 20 });

  return (
    <div>
      <PageHeader title="Tələblər" subtitle="Bütün sığorta tələbləri" />
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        totalPages={data?.totalPages ?? 0}
        currentPage={page}
        onPageChange={setPage}
        isLoading={isLoading}
      />
    </div>
  );
}
```

### 7.10 Role-Based Component Render

```tsx
// src/hooks/use-permissions.ts
import { useAuthStore } from '@/stores/auth.store';
import { ROLES } from '@/config/roles.config';

export function usePermissions() {
  const user = useAuthStore((s) => s.user);
  const roles = user?.roles ?? [];

  return {
    isAdmin: roles.includes(ROLES.ADMIN),
    isPatient: roles.includes(ROLES.PATIENT),
    isDoctor: roles.includes(ROLES.DOCTOR),
    isAgent: roles.includes(ROLES.AGENT),
    isInsuranceAdmin: roles.includes(ROLES.INSURANCE_ADMIN),
    isInsuranceStaff: roles.includes(ROLES.INSURANCE_STAFF),
    isHospitalAdmin: roles.includes(ROLES.HOSPITAL_ADMIN),
    isHospitalStaff: roles.includes(ROLES.HOSPITAL_STAFF),
    hasAnyRole: (...r: string[]) => r.some((role) => roles.includes(role)),
    canManageClaims: roles.some((r) =>
      [ROLES.ADMIN, ROLES.INSURANCE_ADMIN, ROLES.AGENT].includes(r)
    ),
    canViewFraud: roles.some((r) =>
      [ROLES.ADMIN, ROLES.INSURANCE_ADMIN, ROLES.AGENT, ROLES.HOSPITAL_ADMIN].includes(r)
    ),
  };
}

// RoleGate component
interface RoleGateProps {
  allowedRoles: string[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function RoleGate({ allowedRoles, children, fallback = null }: RoleGateProps) {
  const user = useAuthStore((s) => s.user);
  const hasAccess = allowedRoles.some((role) => user?.roles?.includes(role));
  return hasAccess ? <>{children}</> : <>{fallback}</>;
}
```

---

## 8. Global State Management Planı

| Store | Məlumat | Persist |
|---|---|---|
| `auth.store` | accessToken, refreshToken, user, roles | ✅ localStorage |
| `theme.store` | mode (dark/light), accent color | ✅ localStorage |
| `sidebar.store` | isCollapsed, isMobileOpen | ❌ |
| React Query | Bütün server data (policies, claims, etc.) | ✅ (query cache) |

**Prinsip:** Server state → React Query, Client state → Zustand. Redux istifadə olunmur — lazımsız complexity.

---

## 9. Form Validation Strategiyası

Hər form üçün Zod schema yaradılır. Backend DTO-ları ilə 1:1 uyğunluq saxlanılır:

```typescript
// Nümunə: InsuranceCompany create form
const createCompanySchema = z.object({
  name: z.string().min(2, 'Ad minimum 2 simvol olmalıdır').max(255),
  taxId: z.string().min(1, 'VÖEN tələb olunur'),
  licenseNumber: z.string().min(1, 'Lisenziya nömrəsi tələb olunur'),
  email: z.string().email('Düzgün email daxil edin').optional().or(z.literal('')),
  phone: z.string().optional(),
  address: z.object({
    street: z.string().optional(),
    city: z.string().optional(),
    zip: z.string().optional(),
  }).optional(),
});
```

---

## 10. Error / Loading / Empty State Planı

| State | Komponent | İstifadə yeri |
|---|---|---|
| Loading | `<LoadingSkeleton />` | Table, card, page load |
| Loading (inline) | `<Spinner />` | Button submit, modal |
| Error (page) | `<ErrorState message={...} onRetry={...} />` | Query error |
| Error (boundary) | `<ErrorBoundary />` | Unhandled JS errors |
| Error (toast) | `toast.error(...)` | Mutation error |
| Empty | `<EmptyState icon={...} title="..." />` | Table no data |
| 404 | `<NotFoundPage />` | Invalid route |
| 403 | `<ForbiddenPage />` | Role unauthorized |
| 500 | `<ServerErrorPage />` | Server error |

---

## 11. UI/UX Dizayn Sistemi

### Rəng Paleti
- **Primary:** Emerald (sağlamlıq/insurance teması) — `emerald-500` / `emerald-600`
- **Accent:** Blue — `blue-500`
- **Success:** Green — `green-500`
- **Warning:** Amber — `amber-500`
- **Error:** Red — `red-500`
- **Neutral:** Slate — `slate-50` → `slate-900`

### Dark Mode
Tailwind `dark:` prefiksi ilə. `useThemeStore` localStorage-da saxlanır. `<html>` class toggle.

### Responsive Breakpoints
- Mobile: `< 768px` — sidebar drawer, stacked layout
- Tablet: `768px – 1024px` — collapsed sidebar
- Desktop: `> 1024px` — full sidebar

### Typography
- Google Fonts: **Inter** (body), **JetBrains Mono** (monospace/numbers)
- Heading hierarchy: `text-2xl` → `text-lg` → `text-base`

---

## 12. Backend-də Çatışmayan / Əlavə Edilməli Hissələr

> [!WARNING]
> Aşağıdakılar backend-də hazırda mövcud deyil. Frontend development zamanı bu hissələr ya mock olunmalı, ya da backend-ə əlavə edilməlidir.

| # | Çatışan hissə | Frontend ehtiyacı | Prioritet |
|---|---|---|---|
| 1 | **Dashboard statistika endpoint-ləri** | Hər role üçün aggregate stats (policy count, claim count, revenue) lazımdır. Backend-də `/api/v1/dashboard/stats` kimi endpoint yoxdur. | 🔴 Yüksək |
| 2 | **User avatar upload** | Profile səhifələrində avatar lazımdır. Backend-də profil şəkli upload endpoint yoxdur. | 🟡 Orta |
| 3 | **In-app notification WebSocket/SSE** | Real-time notification üçün WebSocket və ya SSE endpoint lazımdır. Hal-hazırda yalnız REST poll. | 🟡 Orta |
| 4 | **Activity log / Audit trail** | Admin paneldə istifadəçi fəaliyyətlərini göstərmək üçün audit endpoint lazımdır. | 🟢 Aşağı |
| 5 | **Search / Global search endpoint** | Bütün entity-lər üzrə qlobal axtarış üçün vahid endpoint yoxdur. | 🟢 Aşağı |
| 6 | **Notification preferences** | İstifadəçinin hansı notification-ları almaq istədiyini idarə etmək üçün endpoint yoxdur. | 🟢 Aşağı |
| 7 | **Export (CSV/PDF)** | Table data-nı export etmək üçün backend endpoint yoxdur. Frontend-dən client-side export mümkündür. | 🟢 Aşağı |
| 8 | **Notification mark-as-read** | Notification oxundu kimi işarələmək üçün PATCH endpoint lazımdır. | 🟡 Orta |
| 9 | **Bulk operations** | Toplu status dəyişikliyi, toplu approve/reject endpoint-ləri yoxdur. | 🟢 Aşağı |

---

## 13. Frontend Development Roadmap

### Phase 1 — Foundation (1-2 həftə)
- [ ] Vite + React + TypeScript project setup
- [ ] Tailwind CSS v4 configuration
- [ ] Folder structure setup
- [ ] Axios client + interceptors
- [ ] Auth store (Zustand)
- [ ] React Query setup
- [ ] Auth pages (Login, Register, Forgot/Reset Password)
- [ ] Auth flow (JWT, refresh token)
- [ ] DashboardLayout (Sidebar, Topbar)
- [ ] AuthGuard, RoleGuard
- [ ] Theme toggle (dark/light)
- [ ] Base UI components (Button, Input, Card, Badge, etc.)

### Phase 2 — Core Pages (2-3 həftə)
- [ ] Dashboard pages (role-based)
- [ ] Profile pages (Patient, Doctor, Agent — create/view/edit)
- [ ] Insurance Company management (CRUD + staff + agents)
- [ ] Hospital management (CRUD + branches + staff + doctors)
- [ ] Insurance Products & Coverage Rules
- [ ] DataTable component (server-side pagination)
- [ ] StatusBadge, AmountDisplay, DateDisplay components
- [ ] EmptyState, LoadingSkeleton components

### Phase 3 — Business Flow (2-3 həftə)
- [ ] Policy module (list, detail, issue, my policies)
- [ ] Eligibility check page
- [ ] Claim module (create, items, documents, submit, list, detail)
- [ ] Claim review module (start, approve, reject, request docs)
- [ ] Payment module (premium, payout, mock flow, list, detail)
- [ ] Invoice module (CRUD + lifecycle)
- [ ] Provider Contract module

### Phase 4 — Advanced Features (1-2 həftə)
- [ ] Health Records module (create, list, detail, treatments)
- [ ] Medical Document upload (MinIO presigned URL flow)
- [ ] Fraud Detection dashboard & detail
- [ ] AI Risk dashboard & detail
- [ ] Notification center (my notifications)
- [ ] Notification templates (ADMIN)

### Phase 5 — Polish & Production (1 həftə)
- [ ] Error boundaries
- [ ] 404, 403, 500 error pages
- [ ] Settings pages (change password, preferences)
- [ ] Toast notification system
- [ ] Responsive refinement (mobile, tablet)
- [ ] Performance optimization (lazy loading, memoization)
- [ ] SEO meta tags
- [ ] Accessibility audit
- [ ] README + deployment docs

---

## Open Questions

> [!IMPORTANT]
> **1. Dashboard Statistika API-si:** Backend-də dashboard üçün aggregate endpoint-lər hazırda mövcud deyil. Frontend-dən mövcud list endpoint-ləri çağırıb client-side aggregation edək, yoxsa backend-ə yeni statistika endpoint-ləri əlavə edilsin?

> [!IMPORTANT]
> **2. Real-time Notifications:** Backend-dəki notification sistemi Kafka event-lərə əsaslanır. Frontend üçün WebSocket/SSE istəyirsiniz, yoxsa sadəcə polling yanaşması kifayətdir?

> [!IMPORTANT]
> **3. Tailwind CSS Versiyası:** Tailwind CSS v4 (yeni CSS-first configuration) istifadə edək, yoxsa v3 (klassik `tailwind.config.js`) ilə davam edək? v4 daha modern amma bəzi ecosystem plugin-ləri hələ adapt olmayıb.

> [!IMPORTANT]
> **4. Multi-language dəstəyi:** Frontend Azərbaycan dilində olacaq, yoxsa i18n (AZ + EN + RU) dəstəyi lazımdır?

> [!IMPORTANT]
> **5. Deployment target:** Frontend Vercel, Netlify, Docker container, yoxsa API Gateway ilə eyni server-də host olunacaq?
