# Notification Service

Base URL: `{{notification_url}}` (`http://localhost:8088`)

Purpose: notification dispatch, user/company notification listing and notification templates.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/notifications/send` | Send notification request |
| GET | `/notifications/{id}` | Get notification by ID |
| GET | `/notifications/my` | Get current user's notifications |
| GET | `/notifications/by-user/{userId}` | Get notifications by user |
| GET | `/notifications/by-company/{companyId}` | Get notifications by company |
| POST | `/notifications/templates` | Create notification template |
| GET | `/notifications/templates` | List notification templates |
| PUT | `/notifications/templates/{id}` | Update notification template |
| PATCH | `/notifications/templates/{id}/status` | Change template status |

## Postman Notes

Use `{{notification_id}}`, `{{template_id}}`, `{{patient_user_id}}` and `{{company_id}}` collection variables for path parameters.
