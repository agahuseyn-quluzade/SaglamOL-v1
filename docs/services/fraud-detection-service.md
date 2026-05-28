# Fraud Detection Service

Base URL: `{{fraud_url}}` (`http://localhost:8087`)

Purpose: fraud checks, fraud assessment lookup and scoped fraud summaries.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/fraud/claims/{claimId}/check` | Run fraud check for claim |
| GET | `/fraud/claims/{claimId}` | Get latest fraud assessment by claim |
| GET | `/fraud/assessments/{assessmentId}` | Get fraud assessment by ID |
| GET | `/fraud/companies/{companyId}/summary` | Get company fraud summary |
| GET | `/fraud/hospitals/{hospitalId}/summary` | Get hospital fraud summary |

## Postman Notes

Use `{{claim_id}}`, `{{fraud_assessment_id}}`, `{{company_id}}` and `{{hospital_id}}` collection variables for path parameters.
