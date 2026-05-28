# P17 Health Record MinIO API

## Configuration

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket:
    medical-documents: medical-documents

saglamol:
  security:
    internal-auth:
      secret: <set-INTERNAL_SERVICE_SECRET>

outbox:
  topic: health-record.events
```

## Upload Flow

1. Initiate upload:

```http
POST /api/v1/health-records/{healthRecordId}/documents/uploads
Content-Type: application/json

{
  "claimId": "00000000-0000-0000-0000-000000000001",
  "patientProfileId": "00000000-0000-0000-0000-000000000002",
  "hospitalId": "00000000-0000-0000-0000-000000000003",
  "documentType": "INVOICE",
  "fileName": "invoice.pdf",
  "fileSize": 1024,
  "contentType": "application/pdf"
}
```

Response contains `uploadUrl`, `documentId`, `storageBucket`, `minioKey`, and `expiresAt`. Upload the file bytes to `uploadUrl` with HTTP `PUT`.

2. Confirm upload:

```http
PUT /api/v1/health-records/{healthRecordId}/documents/{documentId}/confirm
Content-Type: application/json

{
  "sha256Hash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
}
```

Confirming writes `DocumentHashIndex` and stores `MedicalDocumentConfirmedEvent` in `outbox_events`.

## Download Flow

```http
GET /api/v1/health-records/{healthRecordId}/documents/{documentId}?reason=Claim review
```

Response contains document metadata and a 5 minute presigned `downloadUrl`. Access is written to `health_access_log`.

## Internal API

All internal requests require `X-Internal-Service-Secret`.

```http
GET /internal/v1/health-records/documents/{documentId}/hash
GET /internal/v1/health-records/documents/by-claim/{claimId}
GET /internal/v1/health-records/documents/{documentId}/summary
```
