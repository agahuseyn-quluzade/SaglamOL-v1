# SaglamOL Fast Working Tracker

Esas prompt fayli: [fast_working_prompts.md](fast_working_prompts.md)

## P0 - Runtime contract fixes
- [ ] Auth contract: frontend login/register/me backend IAM DTO-larina uygun olsun
- [ ] Password reset endpointleri `/reset-request` ve `/reset-confirm` olsun
- [ ] Role redirect `GET /api/v1/iam/me` response-dan hesablansin

## P1 - Frontend API client layer
- [ ] Policy, payment, health-record client-leri backend controller-lere uygunlasdirilsin
- [ ] Notification, fraud, ai-risk, insurance-products, invoices, provider-contracts client-leri yaransin
- [ ] `src/lib/api/index.ts` butun client-leri export etsin

## P2 - Real data hooks
- [ ] `useMockQuery` portal sehifelerinden cixarilsin
- [ ] Domain hook-lari yaransin: claims, policies, payments, profiles, health records, notifications, fraud, ai-risk, admin
- [ ] Loading/error/empty state pattern-i her data sehifesinde olsun

## P3 - Portal pages
- [ ] Patient portal real API data ile islesin
- [ ] Hospital portal real API data ile islesin
- [ ] Insurance portal real API data ile islesin
- [ ] Admin portal real API data ile islesin

## P4 - Backend small gaps
- [ ] Notification mark-as-read/unread-count endpointleri
- [ ] Claim resubmit endpointi
- [ ] Claim payout retry endpointi ve ya frontend-den gizletme

## P5 - Deploy readiness
- [ ] Frontend Dockerfile + `.dockerignore`
- [ ] Frontend `/api/health`
- [ ] `SaglamOL/docker-compose.yml` frontend service (`3001:3000`)
- [ ] Gateway CORS origins-a `http://localhost:3001`

## P6 - Verification
- [x] Frontend typecheck current state: `npm.cmd run typecheck`
- [ ] Backend smoke test: `scripts/docker-smoke-test.ps1`
- [ ] Full E2E demo flow: patient -> claim -> review -> payout -> notification

## External providers later
- Payment provider
- SMTP/email provider
- SMS provider
- OpenAI-compatible AI provider key
- Production object storage
- Domain/TLS/reverse proxy
- Secret manager
