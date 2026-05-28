# Payment Service

Base URL: `{{payment_url}}` (`http://localhost:8089`)

Purpose: policy premium payments, claim payouts, mock payment status changes and invoice lifecycle.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/payments/policy-premium` | Create policy premium payment |
| POST | `/api/v1/payments/claim-payout` | Create claim payout |
| POST | `/api/v1/payments/{id}/complete-mock` | Complete mock payment |
| POST | `/api/v1/payments/{id}/fail-mock` | Fail mock payment |
| POST | `/api/v1/payments/{id}/refund-mock` | Refund mock payment |
| GET | `/api/v1/payments/{id}` | Get payment by ID |
| GET | `/api/v1/payments/my` | Get current user's payments |
| GET | `/api/v1/payments/by-policy` | Get payments by policy |
| GET | `/api/v1/payments/by-claim` | Get payments by claim |
| GET | `/api/v1/payments/by-company` | Get payments by company |
| GET | `/api/v1/payments/by-hospital` | Get payments by hospital |
| POST | `/api/v1/invoices` | Create invoice |
| GET | `/api/v1/invoices/{id}` | Get invoice by ID |
| GET | `/api/v1/invoices/by-company` | Get invoices by company |
| GET | `/api/v1/invoices/by-hospital` | Get invoices by hospital |
| POST | `/api/v1/invoices/{id}/issue` | Issue invoice |
| POST | `/api/v1/invoices/{id}/mark-paid` | Mark invoice paid |
| POST | `/api/v1/invoices/{id}/cancel` | Cancel invoice |

## Postman Notes

Use `{{payment_id}}`, `{{invoice_id}}`, `{{policy_id}}`, `{{claim_id}}`, `{{company_id}}` and `{{hospital_id}}` collection variables for path parameters.
