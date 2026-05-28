# Health Record Service

Base URL: `{{health_url}}` (`http://localhost:8085`)

Purpose: health records, treatments, medical documents, upload confirmation and document hash lookup.

## Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/health-records` | Create health record |
| GET | `/api/v1/health-records/my` | Get current patient's health records |
| GET | `/api/v1/health-records/{healthRecordId}` | Get health record and write access log |
| GET | `/api/v1/health-records/by-claim/{claimId}` | Get health records by claim |
| POST | `/api/v1/health-records/{healthRecordId}/treatments` | Add treatment to health record |
| PATCH | `/api/v1/health-records/{healthRecordId}/archive` | Archive health record |
| POST | `/api/v1/health-records/{healthRecordId}/documents/uploads` | Initiate medical document upload |
| PUT | `/api/v1/health-records/{healthRecordId}/documents/{documentId}/confirm` | Confirm medical document upload |
| GET | `/api/v1/health-records/{healthRecordId}/documents/{documentId}` | Get medical document |
| DELETE | `/api/v1/health-records/{healthRecordId}/documents/{documentId}` | Delete medical document |

## Internal APIs

Internal calls require `X-Internal-Service-Secret: {{internal_service_secret}}`.

| Method | Path |
| --- | --- |
| POST | `/internal/v1/documents/hash/batch` |
| GET | `/internal/v1/health-records/documents/{documentId}/hash` |
| GET | `/internal/v1/health-records/documents/by-claim/{claimId}` |
| GET | `/internal/v1/health-records/documents/{documentId}/summary` |
