# Prompt 22 - Document Hash Cross-check

## Flow

1. Claim Service accepts nullable `documentId` on claim items and attached claim document references.
2. On submit, Claim Service publishes `ClaimSubmittedEvent.documentIds` with the distinct union of:
   - `claim_items.document_id`
   - `claim_document_references.document_id`
3. Fraud Detection consumes `ClaimSubmittedEvent` asynchronously and sends the event document IDs to Health Record Service:
   - `POST /internal/v1/documents/hash/batch`
   - body: `{ "documentIds": ["..."] }`
4. Health Record Service validates `X-Internal-Service-Secret` and returns confirmed document hash metadata:
   - `documentId`
   - `sha256Hash`
   - `patientProfileId`
   - `claimId`
   - `hospitalId`
5. Fraud Detection compares every non-empty hash with its local `document_hash_index`.
6. If the same hash exists for another claim, `DuplicateDocumentRule` emits a `DUPLICATE_DOCUMENT` signal.
7. After the fraud assessment finishes, all current claim hashes are persisted into Fraud Detection `document_hash_index`.

## Internal API

```http
POST /internal/v1/documents/hash/batch
X-Internal-Service-Secret: <secret>
Content-Type: application/json

{
  "documentIds": [
    "8d730a4a-fcc9-4c45-9c93-0fbe2d5d2c4f"
  ]
}
```

Response:

```json
[
  {
    "documentId": "8d730a4a-fcc9-4c45-9c93-0fbe2d5d2c4f",
    "sha256Hash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "patientProfileId": "7b5e5ad4-cf3d-4d13-9b20-df6c3e13d6bb",
    "claimId": "c615a4f7-8560-4b66-b385-5036761ee332",
    "hospitalId": "b3ce8368-9069-4f3e-aa9f-14d7827cfdb8"
  }
]
```

## Fallback

Manual fraud checks may not have event document IDs. In that case Fraud Detection falls back to Health Record's claim document summary endpoint and then uses the same batch hash endpoint.

## Test Commands

```powershell
.\gradlew.bat :services:claim-service:test :services:health-record-service:test :services:fraud-detection-service:test
.\gradlew.bat test
```
