# Insurance Company Data Layer

Bu sənəd Prompt 5 nəticəsini təsvir edir. Mərhələ yalnız data layer üçündür:
entity, Liquibase, repository, DTO və mapper səviyyəsində dəyişikliklər edilib.
Service/controller/business logic bu mərhələyə daxil edilməyib.

## Məqsəd

SaglamOL çox şirkətli insurance platformasına keçdiyi üçün User Profile
Service daxilində sığorta şirkəti modeli əlavə edildi. Bu model sonrakı
mərhələlərdə policy, claim, payment və agent ownership logic üçün əsas olacaq.

## IAM Role Seed

IAM Service Liquibase changelog-una yeni roles əlavə edildi:

- `INSURANCE_ADMIN`
- `INSURANCE_STAFF`

Changeset:

```text
services/iam-service/src/main/resources/db/changelog/db.changelog-master.xml
006-add-insurance-company-roles
```

Mövcud rollar saxlanılıb:

- `ADMIN`
- `PATIENT`
- `DOCTOR`
- `AGENT`
- `HOSPITAL_ADMIN`
- `HOSPITAL_STAFF`
- `SYSTEM`

`common-security` daxilində `RoleConstants` yeniləndi:

- `INSURANCE_ADMIN = "INSURANCE_ADMIN"`
- `INSURANCE_STAFF = "INSURANCE_STAFF"`

## User Profile Entity Model

Əlavə olunan entity-lər:

- `InsuranceCompany`
- `InsuranceCompanyStaffProfile`

Əlavə olunan enum-lar:

- `InsuranceCompanyStatus`
- `InsuranceCompanyStaffRoleType`
- `InsuranceCompanyStaffStatus`

### InsuranceCompany

Field-lər:

- `id`
- `name`
- `taxId`
- `licenseNumber`
- `email`
- `phone`
- `address`
- `status`
- `createdAt`
- `updatedAt`

Status dəyərləri:

- `PENDING`
- `ACTIVE`
- `SUSPENDED`
- `TERMINATED`

### InsuranceCompanyStaffProfile

Field-lər:

- `id`
- `iamUserId`
- `insuranceCompanyId`
- `roleType`
- `position`
- `employeeCode`
- `status`
- `createdAt`
- `updatedAt`

Role type dəyərləri:

- `INSURANCE_ADMIN`
- `INSURANCE_STAFF`
- `AGENT`

Status dəyərləri:

- `ACTIVE`
- `SUSPENDED`
- `TERMINATED`

## AgentProfile Dəyişikliyi

`AgentProfile` entity-sinə əlavə olundu:

```text
insuranceCompanyId UUID NOT NULL
```

Bu dəyişiklik agent-in konkret insurance company scope-una bağlanması üçün
edildi. Gələcək mərhələdə agent search, policy creation və claim review
authorization bu field əsasında company-scoped olacaq.

Qeyd: Əgər local DB-də artıq `insurance_company_id` olmayan agent row-ları
varsa, Liquibase migration fail edə bilər. Bunun səbəbi column-un `NOT NULL`
olmasıdır. Növbəti mərhələdə agent-company link və ya data migration qərarı
ayrıca verilməlidir.

## Repository-lər

Əlavə olunan repository-lər:

- `InsuranceCompanyRepository`
- `InsuranceCompanyStaffProfileRepository`

Yenilənən repository:

- `AgentProfileRepository`

### InsuranceCompanyRepository

Metodlar:

- `findByTaxId`
- `findByLicenseNumber`
- `findByStatus`

### InsuranceCompanyStaffProfileRepository

Metodlar:

- `findByIamUserId`
- `findByIamUserIdAndInsuranceCompanyId`
- `findAllByInsuranceCompanyId`

### AgentProfileRepository

Əlavə olunan metodlar:

- `findByInsuranceCompanyId`
- `findByIamUserIdAndInsuranceCompanyId`

## DTO-lar

Əlavə olunan request DTO-lar:

- `CreateInsuranceCompanyRequest`
- `UpdateInsuranceCompanyRequest`
- `CreateInsuranceCompanyStaffRequest`
- `LinkAgentToCompanyRequest`

Əlavə olunan response DTO-lar:

- `InsuranceCompanyResponse`
- `InsuranceCompanyStaffResponse`
- `AgentCompanyResponse`

Yenilənən response DTO:

- `AgentProfileResponse`

`AgentProfileResponse` artıq `insuranceCompanyId` qaytarır.

## Mapper-lər

MapStruct dependency User Profile Service-ə əlavə edildi.

Əlavə olunan mapper-lər:

- `InsuranceCompanyMapper`
- `InsuranceCompanyStaffMapper`
- `AgentMapper`

Mövcud `ProfileMapper` yalnız `AgentProfileResponse` dəyişikliklərinə uyğun
yeniləndi.

## Liquibase

User Profile Service changelog-a yeni changeset əlavə edildi:

```text
services/user-profile-service/src/main/resources/db/changelog/db.changelog-master.xml
005-add-insurance-company-model
```

Yaradılan cədvəllər:

- `insurance_companies`
- `insurance_company_staff_profiles`

`agent_profile` cədvəlinə əlavə olunan column:

- `insurance_company_id`

Constraint-lər:

- `uk_insurance_companies_tax_id`
- `uk_insurance_companies_license_number`
- `fk_insurance_staff_company`
- `uk_insurance_staff_user_company`
- `uk_insurance_staff_company_employee`
- `fk_agent_profile_insurance_company`

Index-lər:

- `idx_insurance_companies_status`
- `idx_insurance_company_staff_company`
- `idx_insurance_company_staff_iam_user`
- `idx_agent_profile_insurance_company`

Rollback block əlavə edilib və bu obyektləri geri silir.

## Dəyişən Əsas Fayllar

- `common/common-security/src/main/java/az/saglamol/common/security/RoleConstants.java`
- `services/iam-service/src/main/resources/db/changelog/db.changelog-master.xml`
- `services/user-profile-service/build.gradle`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/entity/InsuranceCompany.java`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/entity/InsuranceCompanyStaffProfile.java`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/entity/AgentProfile.java`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/repository/InsuranceCompanyRepository.java`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/repository/InsuranceCompanyStaffProfileRepository.java`
- `services/user-profile-service/src/main/java/az/saglamol/userprofile/repository/AgentProfileRepository.java`
- `services/user-profile-service/src/main/resources/db/changelog/db.changelog-master.xml`

## Verification

İşlədilən command:

```powershell
.\gradlew.bat build
```

Nəticə:

```text
BUILD SUCCESSFUL
```

## Növbəti Mərhələ

Növbəti prompt service/controller/test mərhələsi olmalıdır:

- Insurance company create/update/read/search API
- Insurance company staff create/list API
- Agent-company link flow
- Insurance company ownership authorization
- ADMIN global access
- INSURANCE_ADMIN own-company access
- INSURANCE_STAFF scoped read access
- Testlər
