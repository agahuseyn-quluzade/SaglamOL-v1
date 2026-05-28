# User Profile Service

Base URL: `{{profile_url}}` (`http://localhost:8082`)

Purpose: patient, doctor, agent, hospital and insurance company profile management.

## Profile APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/profiles/patients` | Create patient profile |
| GET | `/api/v1/profiles/patients/me` | Get my patient profile |
| GET | `/api/v1/profiles/patients/{id}` | Get patient profile |
| PUT | `/api/v1/profiles/patients/{id}` | Update patient profile |
| GET | `/api/v1/profiles/patients/search` | Search patient profiles |
| POST | `/api/v1/profiles/doctors` | Create doctor profile |
| GET | `/api/v1/profiles/doctors/me` | Get my doctor profile |
| GET | `/api/v1/profiles/doctors/{id}` | Get doctor profile |
| PUT | `/api/v1/profiles/doctors/{id}` | Update doctor profile |
| GET | `/api/v1/profiles/doctors/search` | Search doctor profiles |
| POST | `/api/v1/profiles/agents` | Create agent profile |
| GET | `/api/v1/profiles/agents/me` | Get my agent profile |
| GET | `/api/v1/profiles/agents/{id}` | Get agent profile |
| PUT | `/api/v1/profiles/agents/{id}` | Update agent profile |
| GET | `/api/v1/profiles/agents/search` | Search agent profiles |
| PATCH | `/api/v1/profiles/agents/{agentProfileId}/insurance-company/{companyId}` | Link agent to insurance company |
| GET | `/api/v1/profiles/agents/by-company/{companyId}` | Get agents by company |

## Organization APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/profiles/hospitals` | Create hospital |
| GET | `/api/v1/profiles/hospitals` | List hospitals |
| GET | `/api/v1/profiles/hospitals/{hospitalId}` | Get hospital |
| PUT | `/api/v1/profiles/hospitals/{hospitalId}` | Update hospital |
| PATCH | `/api/v1/profiles/hospitals/{hospitalId}/status` | Change hospital status |
| POST | `/api/v1/profiles/hospitals/{hospitalId}/branches` | Create hospital branch |
| GET | `/api/v1/profiles/hospitals/{hospitalId}/branches` | List hospital branches |
| POST | `/api/v1/profiles/hospitals/{hospitalId}/staff` | Create hospital staff |
| GET | `/api/v1/profiles/hospitals/{hospitalId}/staff` | List hospital staff |
| POST | `/api/v1/profiles/hospitals/{hospitalId}/doctors/{doctorProfileId}` | Assign doctor to hospital |
| GET | `/api/v1/profiles/hospitals/{hospitalId}/doctors` | List hospital doctors |
| POST | `/api/v1/insurance-companies` | Create insurance company |
| GET | `/api/v1/insurance-companies` | List insurance companies |
| GET | `/api/v1/insurance-companies/{id}` | Get insurance company |
| PUT | `/api/v1/insurance-companies/{id}` | Update insurance company |
| PATCH | `/api/v1/insurance-companies/{id}/status` | Change insurance company status |
| POST | `/api/v1/insurance-companies/{companyId}/staff` | Create insurance company staff |
| GET | `/api/v1/insurance-companies/{companyId}/staff` | List insurance company staff |
| GET | `/api/v1/insurance-companies/{companyId}/staff/{staffId}` | Get insurance company staff |
| PATCH | `/api/v1/insurance-companies/{companyId}/staff/{staffId}/status` | Change staff status |

## Internal APIs

Internal calls require `X-Internal-Service-Secret: {{internal_service_secret}}`.

| Method | Path |
| --- | --- |
| GET | `/internal/v1/insurance-companies/{companyId}/exists-active` |
| GET | `/internal/v1/users/{iamUserId}/insurance-scope` |
| GET | `/internal/v1/agents/{agentProfileId}/insurance-company` |
| GET | `/internal/v1/insurance-companies/{companyId}/access/current` |
| GET | `/internal/v1/profiles/users/{iamUserId}/summary` |
| GET | `/internal/v1/profiles/patients/{patientProfileId}/exists` |
| GET | `/internal/v1/profiles/doctors/{doctorProfileId}/exists` |
| GET | `/internal/v1/profiles/hospitals/{hospitalId}/exists-active` |
| GET | `/internal/v1/profiles/insurance-companies/{companyId}/exists-active` |
