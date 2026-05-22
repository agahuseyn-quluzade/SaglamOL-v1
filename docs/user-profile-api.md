# User Profile API

User Profile Service patient, doctor ve agent profillerini IAM user identity-den ayri saxlayir. Create ve ownership emeliyyatlari request body-de gelen user id ile deyil, API Gateway-in gonderdiyi `X-User-Id` AuthContext deyeri ile aparilir.

Swagger URL:

```text
http://localhost:8082/swagger-ui/index.html
```

## Security

Butun protected endpoint-lerde Gateway header-leri lazimdir:

```http
X-User-Id: 3f73bff9-6d7d-4ac0-a4c2-332d36e1abcc
X-User-Roles: PATIENT
X-Correlation-Id: local-test-1
```

Qaydalar:

- `PATIENT` yalniz oz patient profilini yarada ve deyise biler.
- `DOCTOR` yalniz oz doctor profilini yarada ve deyise biler.
- `AGENT` yalniz oz agent profilini yarada ve deyise biler.
- `ADMIN` butun profilleri gore ve idare ede biler.
- `HOSPITAL_ADMIN` doctor profile yaratmir; yalniz movcud doctor-u hospital-a assign edir.
- `AGENT` patient ve doctor search ucun read access ala biler.
- `HOSPITAL_ADMIN` doctor search ede biler, amma patient ve agent search access-i yoxdur.

## Patient Example

Request:

```http
POST /api/v1/profiles/patients
Content-Type: application/json
X-User-Id: 3f73bff9-6d7d-4ac0-a4c2-332d36e1abcc
X-User-Roles: PATIENT
```

```json
{
  "firstName": "Aga",
  "lastName": "Huseyn",
  "dateOfBirth": "1995-01-01",
  "gender": "MALE",
  "phone": "+994501234567",
  "email": "aga@saglamol.az",
  "nationalId": "AZE12345678",
  "address": {
    "country": "Azerbaijan",
    "city": "Baku",
    "district": "Narimanov",
    "street": "Ataturk avenue",
    "postalCode": "AZ1000"
  },
  "emergencyContactName": "Ali Huseyn",
  "emergencyContactPhone": "+994501111111",
  "profileStatus": "ACTIVE"
}
```

Response:

```json
{
  "id": "8a62c418-4b27-4d99-8150-bf56919520df",
  "iamUserId": "3f73bff9-6d7d-4ac0-a4c2-332d36e1abcc",
  "firstName": "Aga",
  "lastName": "Huseyn",
  "dateOfBirth": "1995-01-01",
  "gender": "MALE",
  "phone": "+994501234567",
  "email": "aga@saglamol.az",
  "profileStatus": "ACTIVE"
}
```

## Doctor Example

```http
POST /api/v1/profiles/doctors
X-User-Id: 7992c87d-0ade-4bbd-b4d5-dfcd0cc3768c
X-User-Roles: DOCTOR
```

```json
{
  "firstName": "Leyla",
  "lastName": "Aliyeva",
  "specialty": "Cardiology",
  "licenseNumber": "DOC-12345",
  "phone": "+994502222222",
  "email": "leyla.doctor@saglamol.az",
  "address": {
    "country": "Azerbaijan",
    "city": "Baku",
    "district": "Yasamal",
    "street": "Health street",
    "postalCode": "AZ1001"
  },
  "profileStatus": "ACTIVE"
}
```

## Agent Example

```http
POST /api/v1/profiles/agents
X-User-Id: 4bb5576d-e70e-4219-8a5b-a4d5abf758c1
X-User-Roles: AGENT
```

```json
{
  "firstName": "Nigar",
  "lastName": "Mammadova",
  "employeeCode": "AG-1001",
  "department": "Corporate insurance",
  "phone": "+994503333333",
  "email": "nigar.agent@saglamol.az",
  "profileStatus": "ACTIVE"
}
```

## Search

Search endpoint-ler pagination qebul edir:

```http
GET /api/v1/profiles/doctors/search?query=cardio&status=ACTIVE&page=0&size=20&sort=createdAt,desc
X-User-Id: 11111111-1111-1111-1111-111111111111
X-User-Roles: ADMIN
```
