# Prompt 8 - User Profile Final Hardening

This phase finalizes User Profile Service as the shared profile, provider, and organization scope service for the multi-company SaglamOL platform.

## Covered APIs

- Patient profiles: create, own profile, get by id, update, paginated search.
- Doctor profiles: create, own profile, get by id, update, paginated search.
- Agent profiles: create, own profile, get by id, update, paginated search, agents by insurance company.
- Hospitals: create, search, get by id, update, status change, branches, staff, doctor assignment.
- Internal profile queries:
  - `GET /internal/v1/profiles/users/{iamUserId}/summary`
  - `GET /internal/v1/profiles/patients/{patientProfileId}/exists`
  - `GET /internal/v1/profiles/doctors/{doctorProfileId}/exists`
  - `GET /internal/v1/profiles/hospitals/{hospitalId}/exists-active`
  - `GET /internal/v1/profiles/insurance-companies/{companyId}/exists-active`

## Access Matrix

| Role | Patient | Doctor | Agent | Hospital | Insurance company |
| --- | --- | --- | --- | --- | --- |
| ADMIN | Global read/write | Global read/write | Global read/write | Global read/write | Global read/write |
| PATIENT | Own profile read/write | No direct access | No direct access | No management | No management |
| DOCTOR | No patient search | Own profile read/write and assigned provider visibility | No direct access | Assigned hospital visibility | No management |
| AGENT | Patient search/read for business workflows | Doctor search/read | Own profile read/write, own company scope | No management | Own company data through company scope |
| HOSPITAL_ADMIN | No patient management | Doctor search/read for provider workflows | No management | Own hospital write, branch/staff/assignment | No management |
| HOSPITAL_STAFF | No patient management | Doctor read in own hospital/branch scope | No management | Own hospital/branch read | No management |
| INSURANCE_ADMIN | No direct patient management | No provider management | Own company agents through scoped endpoints | No provider management | Own company and staff management |
| INSURANCE_STAFF | No direct patient management | No provider management | Own company scoped read | No provider management | Own company read |

## Hardening Notes

- Ownership is resolved from `AuthContext.userId`, not request body ownership fields.
- Internal endpoints require `X-Internal-Service-Secret`.
- Search filters now support status/name/email plus domain-specific filters: company, hospital, city, and specialty.
- Additional indexes were added for search and organization-scoped agent employee code lookup.
