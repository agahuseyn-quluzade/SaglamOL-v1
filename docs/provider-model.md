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

Authorization gateway-den gelen `X-User-Roles` header-i esasinda yoxlanilir.
