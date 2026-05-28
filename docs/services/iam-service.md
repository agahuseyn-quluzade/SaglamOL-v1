# IAM Service

Base URL: `{{iam_url}}` (`http://localhost:8081`)

Purpose: authentication, token lifecycle, password operations and admin user management.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/iam/register` | Register a patient user |
| POST | `/api/v1/iam/login` | Login by email or phone identifier |
| POST | `/api/v1/iam/login/email` | Login by email |
| POST | `/api/v1/iam/login/phone` | Login by phone number |
| POST | `/api/v1/iam/refresh` | Rotate refresh token and issue a new access token |
| POST | `/api/v1/iam/logout` | Revoke current refresh token |
| POST | `/api/v1/iam/refresh-tokens/revoke` | Revoke a refresh token |
| GET | `/api/v1/iam/me` | Return current authenticated user |
| POST | `/api/v1/iam/password/change` | Change own password |
| POST | `/api/v1/iam/password/reset-request` | Request password reset token |
| POST | `/api/v1/iam/password/reset-confirm` | Confirm password reset with token |
| GET | `/api/v1/iam/users` | List users |
| GET | `/api/v1/iam/users/{id}` | Get user by id |
| GET | `/api/v1/iam/users/search` | Search users by email or phone number |
| PATCH | `/api/v1/iam/users/{id}/status` | Change user status |
| POST | `/api/v1/iam/users/{id}/roles` | Assign role to user |
| DELETE | `/api/v1/iam/users/{id}/roles/{roleName}` | Remove role from user |

## Postman Notes

Use the `IAM Service` folder in `docs/postman/saglamol-services.postman_collection.json`.
Login requests can save `access_token` manually from the response. Authenticated requests use `Authorization: Bearer {{access_token}}`.
