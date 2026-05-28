# AI Risk Service

Base URL: `{{ai_risk_url}}` (`http://localhost:8086`)

Purpose: AI-assisted claim risk assessment and company risk summaries.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/ai-risk/claims/{claimId}/assess` | Run AI risk assessment for claim |
| GET | `/ai-risk/claims/{claimId}` | Get latest AI risk assessment by claim |
| GET | `/ai-risk/assessments/{assessmentId}` | Get AI risk assessment by ID |
| GET | `/ai-risk/companies/{companyId}/summary` | Get company AI risk summary |

## Postman Notes

Use `{{claim_id}}`, `{{ai_assessment_id}}` and `{{company_id}}` collection variables for path parameters.
