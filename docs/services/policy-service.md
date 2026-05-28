# Policy Service

Base URL: `{{policy_url}}` (`http://localhost:8083`)

Purpose: insurance products, coverage rules, provider contracts, policy issuance, eligibility and limit reservations.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/insurance-products` | Create insurance product |
| PUT | `/api/v1/insurance-products/{productId}` | Update insurance product |
| PATCH | `/api/v1/insurance-products/{productId}/status` | Change product status |
| GET | `/api/v1/insurance-products/{productId}` | Get product |
| GET | `/api/v1/insurance-products` | List products |
| POST | `/api/v1/insurance-products/{productId}/coverage-rules` | Create coverage rule |
| PUT | `/api/v1/insurance-products/{productId}/coverage-rules/{ruleId}` | Update coverage rule |
| PATCH | `/api/v1/insurance-products/{productId}/coverage-rules/{ruleId}/status` | Change coverage rule status |
| GET | `/api/v1/insurance-products/{productId}/coverage-rules` | List coverage rules |
| POST | `/api/v1/policies` | Issue policy |
| GET | `/api/v1/policies/{policyId}` | Get policy by ID |
| GET | `/api/v1/policies/me` | Get current patient's policies |
| GET | `/api/v1/policies` | Search policies |
| POST | `/api/v1/policies/eligibility-check` | Check policy eligibility |
| PATCH | `/api/v1/policies/{policyId}/cancel` | Cancel policy |
| PATCH | `/api/v1/policies/{policyId}/suspend` | Suspend policy |
| POST | `/api/v1/provider-contracts` | Create provider contract |
| GET | `/api/v1/provider-contracts/{contractId}` | Get provider contract |
| GET | `/api/v1/provider-contracts/by-company/{companyId}` | List contracts by company |
| GET | `/api/v1/provider-contracts/by-hospital/{hospitalId}` | List contracts by hospital |
| PATCH | `/api/v1/provider-contracts/{contractId}/terminate` | Terminate provider contract |

## Internal APIs

Internal calls require `X-Internal-Service-Secret: {{internal_service_secret}}`.

| Method | Path |
| --- | --- |
| GET | `/internal/v1/policies/{policyId}` |
| GET | `/internal/v1/policies/{policyId}/active` |
| POST | `/internal/v1/policies/eligibility-check` |
| POST | `/internal/v1/policies/{policyId}/limit-reservations` |
| PUT | `/internal/v1/policies/limit-reservations/{reservationId}/confirm` |
| PUT | `/internal/v1/policies/limit-reservations/{reservationId}/release` |
| PUT | `/internal/v1/policies/{policyId}/activate-after-payment` |
