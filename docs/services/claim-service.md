# Claim Service

Base URL: `{{claim_url}}` (`http://localhost:8084`)

Purpose: claim draft creation, item and document attachment, submission, review and internal claim lookup.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/claims` | Create draft claim |
| POST | `/api/v1/claims/{claimId}/items` | Add item to a draft claim |
| POST | `/api/v1/claims/{claimId}/documents` | Attach document reference to a draft claim |
| POST | `/api/v1/claims/{claimId}/submit` | Submit a draft claim |
| GET | `/api/v1/claims/{claimId}` | Get claim by ID |
| GET | `/api/v1/claims/my` | Get current patient's claims |
| GET | `/api/v1/claims` | Search claims by scoped filters |
| POST | `/api/v1/claims/{claimId}/review/start` | Start claim review |
| POST | `/api/v1/claims/{claimId}/review/more-documents` | Request more documents |
| POST | `/api/v1/claims/{claimId}/review/approve` | Approve reviewed claim |
| POST | `/api/v1/claims/{claimId}/review/reject` | Reject reviewed claim |
| PUT | `/api/v1/claims/{claimId}/review/cancel` | Cancel draft or submitted claim |

## Internal APIs

Internal calls require `X-Internal-Service-Secret: {{internal_service_secret}}`.

| Method | Path |
| --- | --- |
| GET | `/internal/v1/claims/{claimId}` |
| GET | `/internal/v1/claims/{claimId}/summary` |
| GET | `/internal/v1/claims/by-patient/{patientProfileId}/summary` |
| GET | `/internal/v1/claims/by-company/{companyId}/summary` |
| GET | `/internal/v1/claims/by-hospital/{hospitalId}/summary` |
