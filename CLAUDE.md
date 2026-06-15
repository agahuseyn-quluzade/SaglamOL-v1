# CLAUDE.md — SaglamOL E-Health Insurance Platform

Guidance for Claude Code working in this repository. Read this before making changes.

---

## 1. What this project is

**SaglamOL** is an AI-assisted e-health **insurance** platform for Azerbaijan. A patient
submits a medical **claim**; the system checks **policy eligibility**, runs **AI risk**
scoring and **fraud detection**, an insurance reviewer **approves/rejects**, and a
**payout** is tracked, with **notifications** at each step.

The repo root `d:\E-Health` is **not** a git repo. It contains two independent codebases:

| Path        | Stack                                   | Role                          | Git |
|-------------|-----------------------------------------|-------------------------------|-----|
| `SaglamOL/` | Java 21, Spring Boot 3.3.6, Gradle      | Backend microservices monorepo| yes |
| `EH-FRONT/` | Next.js 15, React 19, TypeScript        | Frontend web app (4 portals)  | no  |

The other root files (`task.md`, `fast_working_prompts.md`, `deploy_ready_prompts.md`,
`frontend_architecture.md`, `files/hazırkı vəziyyət.txt`) are planning/tracking notes.
**`files/hazırkı vəziyyət.txt` is stale** — it describes the backend as "skeleton only,"
but the backend is in fact substantially implemented (see §4). Trust the code, not that file.
`task.md` is the accurate live tracker; the live work is **frontend integration**.

---

## 2. Environment & how to run things

- **OS: Windows.** Shell is **PowerShell**. Use `npm.cmd` (not `npm`) and `.\gradlew.bat`
  (not `./gradlew`). A Bash tool is also available for POSIX scripts.
- Do **not** prefix commands with `cd` into the working dir unnecessarily; if a command is
  stack-specific, run it from the right subfolder (`SaglamOL/` or `EH-FRONT/`).

### Frontend (`EH-FRONT/`)
```powershell
npm.cmd install
npm.cmd run dev          # next dev — http://localhost:3000
npm.cmd run typecheck    # tsc --noEmit  ← ALWAYS run after frontend changes
npm.cmd run lint         # eslint
npm.cmd run build        # next build (standalone needed for Docker — see §6)
```
Set `NEXT_PUBLIC_API_URL=http://localhost:8080` to point at the gateway.

### Backend (`SaglamOL/`)
```powershell
.\gradlew.bat build                 # compile + test all modules
.\gradlew.bat test                  # tests only
.\gradlew.bat :services:iam-service:test   # single module
.\gradlew.bat bootJar               # build jars BEFORE docker build (more stable)
docker compose config --quiet       # validate compose
docker compose up -d --build        # full local stack
docker compose ps
```
Recommended Docker flow (avoids parallel-build EOF errors): **`bootJar` first, then
`docker compose up -d --build`**.

Smoke test:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\docker-smoke-test.ps1 -ResetVolumes -HealthRetries 60 -HealthDelaySeconds 15
```

---

## 3. Service ports & infrastructure

| Service                | Port | Service                | Port |
|------------------------|------|------------------------|------|
| api-gateway            | 8080 | ai-risk-service        | 8086 |
| iam-service            | 8081 | fraud-detection-service| 8087 |
| user-profile-service   | 8082 | notification-service   | 8088 |
| policy-service         | 8083 | payment-service        | 8089 |
| claim-service          | 8084 | discovery-server       | 8761 |
| health-record-service  | 8085 | config-server          | 8888 |

Infra: PostgreSQL (one server, DB-per-service), Redis (6379), Kafka + Zookeeper (9092),
MinIO (9000 API / 9001 console), Prometheus (9090), Grafana (3000).
**Grafana owns port 3000**, so the frontend container must publish `3001:3000`.

Per-service databases: `iam_db`, `user_db`, `policy_db`, `claim_db`, `health_record_db`,
`ai_analysis_db`, `fraud_db`, `notification_db`, `payment_db` (`saglamol`/`postgres` are
just default DataGrip connections). Tables are created by **Liquibase on service startup**,
not by raising Postgres alone.

---

## 4. Backend state — what's actually implemented

All 9 services have real `controller/`, `service/`, `repository/`, `entity/`, and tests
(not skeletons). Confirmed REST surface (all behind the gateway, JWT-validated; gateway
forwards `X-User-Id`, `X-User-Roles`, `X-Correlation-Id` to services):

- **IAM** `/api/v1/iam`: `register`, `login`, `login/email`, `login/phone`, `refresh`,
  `logout`, `refresh-tokens/revoke`, `me`; password `/api/v1/iam/password`:
  `change`, `reset-request`, `reset-confirm`; admin users `/api/v1/iam/users`:
  `{id}`, `search`, `{id}/status`, `{id}/roles`.
- **Policy** `/api/v1/policies`: `{id}`, `eligibility-check`, limit reservations;
  **insurance-products** `/api/v1/insurance-products` (+ coverage-rules).
- **Claim** `/api/v1/claims`: create, `{id}/items`, `{id}/documents`, `{id}/submit`,
  `{id}`, `my`; review `/api/v1/claims/{id}/review`: `start`, `more-documents`,
  `approve`, `reject`, `cancel`; plus company/hospital/patient summaries.
- **Health-record** `/api/v1/health-records`: `my`, `{id}`, `by-claim/{claimId}`,
  treatments, `{id}/archive`; documents via **presigned flow** (`uploads` → client PUT →
  `{documentId}/confirm`).
- **AI-risk** `/ai-risk`: `claims/{id}/assess`, `claims/{id}`, company summary.
- **Fraud** `/fraud`: `claims/{id}/check`, `claims/{id}`, company/hospital summary.
- **Notification** `/notifications`: `send`, `{id}`, `my`, `by-user`, `by-company`;
  templates.
- **Payment** `/api/v1/payments`: `policy-premium`, `claim-payout`, `{id}/complete-mock`,
  `{id}/fail-mock`, `{id}/refund-mock`, lookups; **invoices** `/api/v1/invoices`.

Infra present: Spring Cloud Gateway routing + JWT validation, common modules
(`common-events`, `common-kafka`, `common-security`, `common-exception`), Liquibase
migrations per service, Dockerfiles, full `docker-compose.yml`, Swagger/OpenAPI, MinIO,
Redis, Prometheus/Grafana, a Postman collection (`docs/postman_collection.json`).

**Known backend gaps / hardening (not blocking the MVP):**
- JWT is HMAC today; production target is IAM-signs-with-private-key / gateway-validates-public-key.
- `TrustedContextFilter` + method-level RBAC (`@PreAuthorize`) not uniform across services.
- Outbox publisher / Kafka consumers are partially wired — verify before relying on events.
- App-level `/actuator/health` healthchecks in compose are incomplete.
- Mocked external providers in place: `MockPaymentGatewayClient`, `EmailMockSender`,
  `SmsMockSender`, AI rule-based fallback. Real providers are post-MVP.

---

## 5. Frontend state — the real work lives here

Next.js 15 (App Router), React 19, TanStack Query, Zustand, Axios, Zod, react-hook-form,
recharts, lucide-react, Tailwind-style design system. `npm.cmd run typecheck` currently passes.

**Working:** public pages (`login`, `register`, `apply`, `password-reset/request`,
`password-reset/confirm`), `auth.store` (Zustand) with token refresh interceptor in
`lib/api/client.ts`, role→portal mapping in `lib/auth/roles.ts`, an `/api/proxy/[...path]`
route, and these API clients: `admin`, `claims`, `health-records`, `iam`, `password`,
`payments`, `policies`, `profiles`, `types`, `client`, `index`.

**Not done (the gap):**
- Portal pages are **catch-all `[[...slug]]` routes** (`patient`, `hospital`, `insurance`,
  `admin`) rendering **mock data via `lib/hooks/useMockQuery.ts`** — not real pages.
- **No real domain hooks** exist (only `useMockQuery`).
- **Missing API clients:** `notifications`, `fraud`, `ai-risk`, `insurance-products`,
  `invoices`, `provider-contracts`.
- **Auth/API contract mismatches with the backend** (see §6 — these break login today).

### Roles (frontend `UserRole`)
`PATIENT, DOCTOR, HOSPITAL_ADMIN, HOSPITAL_STAFF, INSURANCE_ADMIN, INSURANCE_STAFF, AGENT,
ADMIN, SYSTEM`. Portal map: PATIENT→patient; DOCTOR/HOSPITAL_*→hospital;
INSURANCE_*/AGENT→insurance; ADMIN/SYSTEM→admin. Primary-role precedence to implement:
`ADMIN > SYSTEM > HOSPITAL_ADMIN > HOSPITAL_STAFF > INSURANCE_ADMIN > INSURANCE_STAFF >
AGENT > DOCTOR > PATIENT`.

---

## 6. Critical contracts & gotchas (verify in code, don't guess)

**Login is broken today** — frontend and backend disagree:

| Concern        | Frontend sends/expects now            | Backend actual                                                |
|----------------|----------------------------------------|---------------------------------------------------------------|
| Login body     | `{ email, password }` → `/iam/login`  | `/iam/login` wants `{ identifier, password }`; use `/iam/login/email` for `{ email, password }` |
| Login response | `{ accessToken, refreshToken, user:{email,fullName,role} }` | `{ accessToken, refreshToken, tokenType, expiresInSeconds }` — **no user/role** |
| Get role       | from login response                    | call `GET /iam/me` → `{ userId, email, phoneNumber, roles: string[] }`, derive primary role |
| Register body  | includes `role`                        | `{ email, phoneNumber?, password }` — **drop `role`**         |
| Password reset | (align)                                | `/iam/password/reset-request`, `/iam/password/reset-confirm`  |

Other alignment rules:
- Health-record document upload is **presigned**, not multipart:
  `POST .../documents/uploads` → client `PUT` to presigned URL → `PUT .../{documentId}/confirm`.
- Policy `cancel`/`suspend` are `PATCH` with `{ reason }` body.
- Claim `resubmit` / `retry-payout` have **no public backend endpoint** — hide those UI
  actions or gate behind a backend gap task; don't invent endpoints.
- When aligning a client, **read the actual controller + DTOs** under
  `SaglamOL/services/**/controller` and `**/dto/{request,response}` — endpoint paths and
  shapes are authoritative there.

**CORS:** gateway reads `CORS_ALLOWED_ORIGINS` (default
`http://localhost:3000,http://localhost:5173,http://localhost:8080`). Add
`http://localhost:3001` when the frontend container is added.

**Demo data:** password for all seeded users is **`Test1234!`**. Seeded emails:
`patient@`, `doctor@`, `agent@`, `hospital-admin@`, `hospital-staff@`, `insurance-admin@`,
`insurance-staff@`, `admin@` (all `@saglamol.az`).

---

## 7. What needs to be done (priority order — mirrors `task.md`)

- **P0 Runtime contract fixes:** fix auth (login/email, `/me`-derived role, drop `role`
  from register), password reset endpoints, role-based redirect.
- **P1 API client layer:** align `policies`, `payments`, `health-records`; add
  `notifications`, `fraud`, `ai-risk`, `insurance-products`, `invoices`,
  `provider-contracts`; export all from `lib/api/index.ts`; tighten `types.ts`.
- **P2 Real data hooks:** replace `useMockQuery` with TanStack Query domain hooks
  (`useAuth`, `useClaims`, `usePolicies`, `usePayments`, `useProfiles`, `useHealthRecords`,
  `useNotifications`, `useFraud`, `useAiRisk`, `useInsuranceProducts`, `useInvoices`,
  `useProviderContracts`, `useAdmin`) with loading/error/empty states.
- **P3 Portal pages:** replace `[[...slug]]` catch-alls with real patient/hospital/
  insurance/admin pages backed by hooks.
- **P4 Small backend gaps:** notification read + unread-count; claim resubmit (from
  `NEEDS_MORE_DOCUMENTS`); claim payout retry (only if status model supports it, else hide).
- **P5 Deploy readiness:** `next.config.ts` `output:"standalone"`; `/api/health` route;
  `EH-FRONT/Dockerfile` + `.dockerignore`; add `frontend` service to
  `SaglamOL/docker-compose.yml` (`3001:3000`, `NEXT_PUBLIC_API_URL=http://api-gateway:8080`,
  depends_on gateway, `saglamol-network`); add `http://localhost:3001` to CORS.
- **P6 Verification:** frontend typecheck + build; backend smoke test; full E2E
  (patient → claim → review → payout → notification).

Full demo flow target: register → patient profile → admin creates plan → agent issues
policy → doctor health record → patient submits claim → eligibility → AI risk → fraud →
approve/reject → payout tracking → notification.

---

## 8. Working agreements

1. **Keep the stack.** No new frameworks or external providers for the MVP — mocks/fallbacks
   already exist (payment, email/SMS, AI). Production providers are a separate phase.
2. **Backend is the source of truth for contracts.** When the frontend disagrees with a
   controller, change the frontend (unless the task is an explicit backend gap in §7-P4).
3. **Mock data only as dev fallback / empty state** — portal pages must render real API data.
4. **Always verify after a change:**
   - Frontend: `npm.cmd run typecheck` (and `npm.cmd run build` for deploy tasks).
   - Backend: `.\gradlew.bat test` (or the specific module test).
5. Commit/push only when asked. Backend git lives in `SaglamOL/`; the repo root and
   `EH-FRONT/` are not git repos — if version control is needed for the frontend, ask first.
6. Reference files as clickable links, e.g. [client.ts](EH-FRONT/src/lib/api/client.ts).

---

## 9. Key references in-repo

- Backend architecture/spec: [ehealth_project_structure.md](SaglamOL/ehealth_project_structure.md),
  [e_health_insurance_backend_codex_project_spec.md](SaglamOL/e_health_insurance_backend_codex_project_spec.md),
  `SaglamOL/docs/` (`architecture.md`, `apis.md`, `claim-lifecycle.md`,
  `local-development.md`, `transactional-outbox-idempotent-consumer.md`, `testing.md`).
- Frontend plan: [frontend_architecture.md](frontend_architecture.md).
- Live task tracker & copy-paste prompts: [task.md](task.md),
  [fast_working_prompts.md](fast_working_prompts.md),
  [deploy_ready_prompts.md](deploy_ready_prompts.md).
- Postman: [postman_collection.json](SaglamOL/docs/postman_collection.json).
