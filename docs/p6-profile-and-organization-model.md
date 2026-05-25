# Prompt 6 - Insurance Company Service, Controller and Tests

Bu mərhələ Prompt 5-də yaradılmış InsuranceCompany data layer üzərində service,
controller və test qatını əlavə edir.

## Əlavə Olunan Service-lər

- `InsuranceCompanyService`
- `InsuranceCompanyStaffService`
- `AgentCompanyService`
- `InsuranceCompanyAccessService`

`InsuranceCompanyAccessService` ownership və role-based access qaydalarını
mərkəzləşdirir. Controller-lərdə business logic yoxdur.

## Authorization Qaydaları

- `ADMIN` bütün insurance company datasını yarada, görə və idarə edə bilər.
- `INSURANCE_ADMIN` yalnız bağlı olduğu insurance company datasını görə və staff idarə edə bilər.
- `INSURANCE_STAFF` yalnız bağlı olduğu insurance company datasını görə bilər.
- `AGENT` yalnız bağlı olduğu insurance company datasını və həmin company agent siyahısını görə bilər.
- `PATIENT`, `DOCTOR`, `HOSPITAL_ADMIN`, `HOSPITAL_STAFF` company management endpoint-lərində `403` alır.

Ownership lookup:

- `INSURANCE_ADMIN` və `INSURANCE_STAFF`: `iamUserId -> InsuranceCompanyStaffProfile -> insuranceCompanyId`
- `AGENT`: `iamUserId -> AgentProfile -> insuranceCompanyId`
- `ADMIN`: global bypass

## Business Qaydaları

- `taxId` unique olmalıdır.
- `licenseNumber` unique olmalıdır.
- Yeni company default `PENDING` status ilə yaranır.
- `iamUserId + insuranceCompanyId` staff üçün unique-dir.
- `employeeCode` company daxilində unique-dir.
- Agent company-yə `AgentCompanyService.linkAgentToCompany(...)` ilə bağlanır.
- Policy/product yaradılması gələcək policy-service mərhələsində yalnız `ACTIVE` company üçün icazəli olmalıdır.

## Endpoint-lər

Insurance company:

- `POST /api/v1/insurance-companies`
- `GET /api/v1/insurance-companies`
- `GET /api/v1/insurance-companies/{id}`
- `PUT /api/v1/insurance-companies/{id}`
- `PATCH /api/v1/insurance-companies/{id}/status?newStatus=ACTIVE`

Insurance company staff:

- `POST /api/v1/insurance-companies/{companyId}/staff`
- `GET /api/v1/insurance-companies/{companyId}/staff`
- `GET /api/v1/insurance-companies/{companyId}/staff/{staffId}`
- `PATCH /api/v1/insurance-companies/{companyId}/staff/{staffId}/status?newStatus=SUSPENDED`

Agent-company link:

- `PATCH /api/v1/profiles/agents/{agentProfileId}/insurance-company/{companyId}`
- `GET /api/v1/profiles/agents/by-company/{companyId}`

## Request Nümunələri

Create company:

```http
POST /api/v1/insurance-companies
```

```json
{
  "name": "Saglam Insurance ASC",
  "taxId": "TAX-1001",
  "licenseNumber": "LIC-1001",
  "email": "office@saglam-insurance.az",
  "phone": "+994501234567",
  "address": {
    "country": "AZ",
    "city": "Baku",
    "district": "Nasimi",
    "street": "Nizami 10",
    "postalCode": "AZ1000"
  }
}
```

Change company status:

```http
PATCH /api/v1/insurance-companies/{id}/status?newStatus=ACTIVE
```

Create staff:

```http
POST /api/v1/insurance-companies/{companyId}/staff
```

```json
{
  "iamUserId": "11111111-1111-1111-1111-111111111111",
  "roleType": "INSURANCE_STAFF",
  "position": "Claims Operator",
  "employeeCode": "EMP-1001"
}
```

Link agent:

```http
PATCH /api/v1/profiles/agents/{agentProfileId}/insurance-company/{companyId}
```

## Test Coverage

Əlavə olunan testlər:

- `ADMIN` company yarada bilir və controller `201` qaytarır.
- Yeni company default `PENDING` status alır.
- Duplicate `taxId` conflict verir.
- Duplicate `licenseNumber` conflict verir.
- `ADMIN` company statusunu `ACTIVE` edə bilir.
- `INSURANCE_ADMIN` öz company datasını görə bilir.
- `INSURANCE_ADMIN` başqa company datasına access edə bilmir.
- `INSURANCE_ADMIN` öz company-sinə staff əlavə edə bilir.
- Agent company-yə link olunur.
- `PATIENT` company management üçün forbidden alır.
- `HOSPITAL_ADMIN` company management üçün forbidden alır.

## Verification

İşlədilən command:

```powershell
.\gradlew.bat :services:user-profile-service:test
```

Nəticə:

```text
BUILD SUCCESSFUL
```

## Qeyd

Prompt 5-də `agent_profile.insurance_company_id` `NOT NULL` kimi əlavə edilib.
Mövcud local DB-də unlinked agent row-ları varsa, migration zamanı data cleanup
və ya transitional migration lazım ola bilər.
