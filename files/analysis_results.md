# SaglamOL Current Analysis

Tarix: 2026-06-12

## Repo Snapshot

- Backend: `SaglamOL`
  - Java 21, Spring Boot 3.3.6, Spring Cloud, Gradle multi-module.
  - 9 business service: IAM, user-profile, policy, claim, health-record,
    ai-risk, fraud-detection, notification, payment.
  - Infrastructure: API Gateway, Config Server, Eureka Discovery.
  - Docker Compose includes PostgreSQL, Redis, Kafka/Zookeeper, MinIO,
    Prometheus, Grafana and all backend services.
  - Local seed users exist when `LIQUIBASE_CONTEXTS=local`.
  - Demo password: `Test1234!`.
  - Tests exist across core services and common modules.

- Frontend: `EH-FRONT`
  - Next.js 15, React 19, TypeScript, Zustand, TanStack Query, Axios, Zod.
  - `npm.cmd run typecheck` passes in current state.
  - Public auth pages exist.
  - Portal pages currently use catch-all routes and mock data through
    `PortalPage` + `useMockQuery`.

## Critical Integration Findings

1. IAM login contract mismatch.
   - Frontend sends `{ email, password }` and expects `user.role` in login
     response.
   - Backend `POST /api/v1/iam/login` accepts `{ identifier, password }`.
   - Backend `POST /api/v1/iam/login/email` accepts `{ email, password }`.
   - Backend login response is token-only.
   - User identity and roles must be fetched from `GET /api/v1/iam/me`.

2. Password reset path mismatch.
   - Frontend uses `/api/v1/iam/password/reset/request`.
   - Backend uses `/api/v1/iam/password/reset-request`.
   - Frontend uses `/api/v1/iam/password/reset/confirm`.
   - Backend uses `/api/v1/iam/password/reset-confirm`.

3. Portal data is not real yet.
   - Patient, hospital, insurance and admin portal pages are rendered by
     `[[...slug]]/page.tsx`.
   - Data comes from `EH-FRONT/src/lib/data/mock.ts`.
   - Real React Query hooks still need to replace mock query usage.

4. API client gaps.
   - Missing or incomplete clients: notifications, fraud, ai-risk,
     insurance-products, invoices, provider-contracts.
   - Existing clients need endpoint/method alignment:
     policy cancel/suspend, health document upload, payments, claims.

5. Frontend deploy gap.
   - No frontend Dockerfile yet.
   - Next config has no `output: "standalone"`.
   - Docker Compose has no frontend service yet.
   - Grafana already uses host port 3000, so frontend compose mapping should be
     `3001:3000`.

## Fastest Working Path

1. Fix frontend auth contract first.
2. Align all frontend API clients with backend controllers.
3. Add real React Query hooks.
4. Replace catch-all mock portal pages with real MVP pages.
5. Add tiny backend endpoint gaps only where UI truly needs them.
6. Dockerize frontend and add it to compose.
7. Run backend smoke test and frontend E2E demo flow.

Detailed prompts are in `fast_working_prompts.md`.

## External Providers Needed Later

Local MVP can work without these because mocks/fallbacks exist. Production needs:

- Real payment provider for premium, payout, refund and webhook flow.
- Email provider or SMTP.
- SMS provider.
- OpenAI-compatible AI provider key, or another compatible model gateway.
- Production object storage such as S3/managed MinIO.
- Domain, TLS and reverse proxy.
- Secret manager or vault.
- Production monitoring, backups and alerting.

