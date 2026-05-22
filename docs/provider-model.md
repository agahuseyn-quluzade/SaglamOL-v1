# Provider Model

SaglamOL-da hospital ve doctor ayri anlayislardir.

## IAM Roles

- `HOSPITAL_ADMIN`: hospital teskilatini, filiallari, staff ve doctor assignment-lari idare edir.
- `HOSPITAL_STAFF`: hospital emeliyyat iscisi kimi tibbi sened, claim review ve health record proseslerinde istirak edir.
- `DOCTOR`: ferdi hekim profilidir ve bir ve ya bir nece hospital-a assign oluna biler.

IAM yalniz identity, role ve permission saxlayir. Hospital biznes datasi `user-profile-service` daxilindedir.

## User Profile Provider Tables

- `hospital`: hospital/provider teskilati.
- `hospital_branch`: hospital filiallari.
- `hospital_staff_profile`: hospital admin/staff user profil elaqesi.
- `doctor_hospital_assignment`: doctor profile ile hospital/branch arasinda elaqe.

## API

Base path:

```text
/api/v1/profiles/hospitals
```

Endpoints:

- `POST /api/v1/profiles/hospitals`
- `GET /api/v1/profiles/hospitals`
- `GET /api/v1/profiles/hospitals/{hospitalId}`
- `POST /api/v1/profiles/hospitals/{hospitalId}/branches`
- `GET /api/v1/profiles/hospitals/{hospitalId}/branches`
- `POST /api/v1/profiles/hospitals/{hospitalId}/staff`
- `GET /api/v1/profiles/hospitals/{hospitalId}/staff`
- `POST /api/v1/profiles/hospitals/{hospitalId}/doctors/{doctorProfileId}`
- `GET /api/v1/profiles/hospitals/{hospitalId}/doctors`

## Provider Ownership Authorization

Authorization yalniz role yoxlamasi deyil. Gateway-den gelen `X-User-Id` ve `X-User-Roles` header-leri `common-security` ile `AuthContext`-e cevrilir, ownership ise `user-profile-service` daxilinde `ProviderAccessService` merkezinde yoxlanilir.

Qaydalar:

- `ADMIN` global bypass huququna malikdir.
- `HOSPITAL_ADMIN` yalniz `X-User-Id -> HospitalStaffProfile -> hospitalId` uygun gelen hospital-i idare ede biler.
- `HOSPITAL_ADMIN` basqa hospital-a branch, staff ve doctor assignment elave ede bilmez.
- `HOSPITAL_STAFF` yalniz oz hospital-ini gore biler; `branchId` varsa branch datasinda hemin branch ile mehdudlasir.
- `DOCTOR` yalniz `X-User-Id -> DoctorProfile -> DoctorHospitalAssignment` ile assignment oldugu hospital/branch datasini gore biler.
- Request path-de gelen `hospitalId` ve `branchId` tekbasina etibarli sayilmir; resource DB-den tapilir ve user-in profile/assignment elaqesi ile yoxlanilir.

Provider authorization ucun merkez metodlar:

- `canManageHospital(userId, hospitalId)`
- `canViewHospital(userId, hospitalId)`
- `canManageBranch(userId, branchId)`
- `canViewBranch(userId, branchId)`
- `canManageHospitalStaff(userId, hospitalId)`
- `canAssignDoctor(userId, hospitalId, doctorProfileId)`
- `canDoctorAccessHospital(userId, hospitalId)`
