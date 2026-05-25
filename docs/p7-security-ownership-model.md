# Prompt 7 - Insurance Company Ownership Authorization

Bu mərhələ insurance company ownership modelini tamamlayır və service-to-service
istifadə üçün internal read API əlavə edir.

Əsas prinsip:

```text
AuthContext.userId -> Profile -> Organization Scope -> Business Resource
```

## Ownership Lookup

`ADMIN`:

```text
Global bypass
```

`INSURANCE_ADMIN` və `INSURANCE_STAFF`:

```text
AuthContext.userId -> InsuranceCompanyStaffProfile -> insuranceCompanyId
```

`AGENT`:

```text
AuthContext.userId -> AgentProfile -> insuranceCompanyId
```

Digər rollar:

```text
Company scope yoxdur
```

## Access Matrix

| Role | View Company | Manage Company | Manage Staff | Agent Operation |
| --- | --- | --- | --- | --- |
| `ADMIN` | All | All | All | All |
| `INSURANCE_ADMIN` | Own company | Own company | Own company | No |
| `INSURANCE_STAFF` | Own company | No | No | No |
| `AGENT` | Own company | No | No | Own company |
| `PATIENT` | No | No | No | No |
| `DOCTOR` | No | No | No | No |
| `HOSPITAL_ADMIN` | No | No | No | No |
| `HOSPITAL_STAFF` | No | No | No | No |

## Access Service

`InsuranceCompanyAccessService` bu metodları təmin edir:

- `canViewCompany(userId, companyId)`
- `canManageCompany(userId, companyId)`
- `canManageCompanyStaff(userId, companyId)`
- `canAgentOperateForCompany(userId, companyId)`
- `getCurrentUserInsuranceCompanyId(AuthContext)`
- `requireCanViewCompany(AuthContext, companyId)`
- `requireCanManageCompany(AuthContext, companyId)`
- `requireAgentOrInsuranceStaffInCompany(AuthContext, companyId)`
- `resolveCompanyScopeForCurrentUser(AuthContext)`

Controller-lərdə ownership logic yoxdur. Endpoint-lər service layer vasitəsilə
bu access service-i istifadə edir.

## Public Business Endpoint Enforcement

Prompt 6-da yaradılan endpoint-lər ownership modelinə bağlıdır:

- `GET /api/v1/insurance-companies/{id}`
- `PUT /api/v1/insurance-companies/{id}`
- `PATCH /api/v1/insurance-companies/{id}/status`
- `POST /api/v1/insurance-companies/{companyId}/staff`
- `GET /api/v1/insurance-companies/{companyId}/staff`
- `GET /api/v1/insurance-companies/{companyId}/staff/{staffId}`
- `PATCH /api/v1/insurance-companies/{companyId}/staff/{staffId}/status`
- `PATCH /api/v1/profiles/agents/{agentProfileId}/insurance-company/{companyId}`
- `GET /api/v1/profiles/agents/by-company/{companyId}`

## Internal API

Base path:

```text
/internal/v1
```

Endpoint-lər:

- `GET /internal/v1/insurance-companies/{companyId}/exists-active`
- `GET /internal/v1/users/{iamUserId}/insurance-scope`
- `GET /internal/v1/agents/{agentProfileId}/insurance-company`
- `GET /internal/v1/insurance-companies/{companyId}/access/current`

Internal endpoint-lər `X-Internal-Service-Secret` header-i tələb edir.

```http
X-Internal-Service-Secret: dev-internal-secret
```

Secret config-dən oxunur:

```yaml
saglamol:
  security:
    internal-auth:
      secret: ${INTERNAL_SERVICE_SECRET:dev-internal-secret}
```

Missing və ya invalid secret üçün cavab:

```text
401 Unauthorized
```

`/internal/v1` path-i `InternalAuthFilter` public-prefix siyahısına əlavə
edilib ki, bu path-ləri JWT/Gateway identity filter yox, internal secret
mexanizmi qorusun. `access/current` endpoint-i əlavə olaraq
`X-User-Id`, `X-User-Roles`, `X-Correlation-Id` header-lərini manual parse edir.

## Internal Response-lar

`InsuranceScopeResponse`:

- `iamUserId`
- `insuranceCompanyId`
- `roles`
- `canView`
- `canManage`
- `isAgent`
- `isInsuranceAdmin`
- `isInsuranceStaff`

`AccessCheckResponse`:

- `iamUserId`
- `companyId`
- `insuranceCompanyId`
- `canView`
- `canManage`
- `canManageStaff`
- `canAgentOperate`

## Liquibase

Yeni changeset:

```text
006-add-insurance-ownership-indexes
```

Əlavə index-lər:

- `idx_insurance_staff_iam_user_company`
- `idx_agent_profile_user_company`

## Tests

Əlavə və genişləndirilmiş testlər:

- `ADMIN` bütün company-lərə access edə bilir.
- `INSURANCE_ADMIN` öz company-sini idarə edə bilir.
- `INSURANCE_ADMIN` başqa company-yə access edə bilmir.
- `INSURANCE_STAFF` öz company-sini görə bilir.
- `INSURANCE_STAFF` staff idarə edə bilmir.
- `AGENT` yalnız öz company-sində əməliyyat edə bilir.
- `PATIENT` forbidden alır.
- `HOSPITAL_ADMIN` forbidden alır.
- Internal endpoint valid secret ilə `200`.
- Internal endpoint invalid secret ilə `401`.

Verification:

```powershell
.\gradlew.bat :services:user-profile-service:test
```

Nəticə:

```text
BUILD SUCCESSFUL
```
