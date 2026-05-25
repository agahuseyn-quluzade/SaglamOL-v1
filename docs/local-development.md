# Local Development

## Docker Runtime Flow

Full runtime stack-i qaldirmaq ucun:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose up -d --build
docker compose ps
```

Bir servisin log-larina baxmaq ucun:

```powershell
docker compose logs -f <service-name>
```

Numune:

```powershell
docker compose logs -f iam-service
```

Stack-i saxlamaq ucun:

```powershell
docker compose down
```

Local volume-lari sifirlamaq ucun:

```powershell
docker compose down -v
```

Yalniz infra lazimdirsa:

```powershell
docker compose up -d postgres redis zookeeper kafka minio
```

Yalniz PostgreSQL lazimdirsa:

```powershell
docker compose up -d postgres
```

Docker Desktop-da paralel image build resurs problemi yarana biler. Ona gore
jar-lar host-da evvelce build olunur, Dockerfile-lar ise hazir boot jar-lari
runtime image-e kopyalayir:

```powershell
.\gradlew.bat bootJar
docker compose up -d --build
```

## Docker Services

Root `docker-compose.yml` override faylsiz butun runtime stack-i qaldirir:

- `postgres`
- `redis`
- `zookeeper`
- `kafka`
- `minio`
- `discovery-server`
- `config-server`
- `api-gateway`
- `iam-service`
- `user-profile-service`
- `policy-service`
- `claim-service`
- `health-record-service`
- `ai-risk-service`
- `fraud-detection-service`
- `notification-service`
- `payment-service`

Butun app service-ler `/actuator/health` esasli Docker healthcheck ile
yoxlanilir. Java app container-lerine Docker Desktop-da stabil runtime ucun
512 MB memory limit ve 384 MB JVM MaxRAM verilib.

## IntelliJ Database Connection

Database panelinde `+ -> Data Source -> PostgreSQL` sec.

Tek PostgreSQL connection ucun:

| Field | Value |
| --- | --- |
| Host | `localhost` |
| Port | `5432` |
| User | `saglamol` |
| Password | `saglamol` |
| Database | `saglamol` |

IntelliJ-de data source adini `SaglamOL` qoy. Connection database kimi
`saglamol` istifade et. Sonra `Schemas` tabinda `All databases` ve ya
gormek istediyin database-leri sec.

Database-ler:

- `saglamol`
- `iam_db`
- `user_db`
- `policy_db`
- `claim_db`
- `health_record_db`
- `ai_analysis_db`
- `fraud_db`
- `notification_db`
- `payment_db`
- `postgres`

Servislerin istifade etdiyi database-ler:

| Service | Host | Port | Database | User | Password |
| --- | --- | --- | --- | --- | --- |
| iam-service | `localhost` | `5432` | `iam_db` | `saglamol` | `saglamol` |
| user-profile-service | `localhost` | `5432` | `user_db` | `saglamol` | `saglamol` |
| policy-service | `localhost` | `5432` | `policy_db` | `saglamol` | `saglamol` |
| claim-service | `localhost` | `5432` | `claim_db` | `saglamol` | `saglamol` |
| health-record-service | `localhost` | `5432` | `health_record_db` | `saglamol` | `saglamol` |
| ai-risk-service | `localhost` | `5432` | `ai_analysis_db` | `saglamol` | `saglamol` |
| fraud-detection-service | `localhost` | `5432` | `fraud_db` | `saglamol` | `saglamol` |
| notification-service | `localhost` | `5432` | `notification_db` | `saglamol` | `saglamol` |
| payment-service | `localhost` | `5432` | `payment_db` | `saglamol` | `saglamol` |

`Connection refused` xetasi adeten Postgres container islemeyende cixir.
Evvel `docker compose up -d postgres` isletdiyine emin ol.

## Active Profile

Servislerde default profile `local`-dir:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

Docker runtime-da compose app service-ler ucun `SPRING_PROFILES_ACTIVE=local`
verir. Config Server native config backend ucun `native,local` profilleri ile
qalxir.

IntelliJ run configuration-da ayrica yazmaq istesen:

```text
SPRING_PROFILES_ACTIVE=local
```

## Config Server Runtime Config

`config-server` native mode ile qalxir ve classpath daxilindeki
`infrastructure/config-server/src/main/resources/config/` qovlugundan
runtime config paylayir. IAM/Auth Service hazirda oz lokal config-i ile
qalir; downstream biznes servisleri Config Server-den config oxuyur.

Config Server-den oxunan servis config fayllari:

- `user-profile-service.yml`
- `policy-service.yml`
- `claim-service.yml`
- `payment-service.yml`
- `health-record-service.yml`
- `ai-risk-service.yml`
- `fraud-detection-service.yml`
- `notification-service.yml`

Bu fayllarda port, datasource, Liquibase changelog, actuator exposure,
correlation id logging pattern, Eureka URL, lazim olan Kafka/Redis/MinIO/AI
parametrleri ve internal service secret saxlanilir. Servislerin oz
`application.yml` fayllarinda yalniz minimal self-config qalir:
application name, `optional:configserver` import, `local` profile fallback
ve basic actuator config.

Docker runtime-da compose her downstream service-e bu endpoint-i verir:

```text
CONFIG_SERVER_URL=http://config-server:8888
```

Host-dan servisi IntelliJ ile ise salanda default fallback budur:

```text
CONFIG_SERVER_URL=http://localhost:8888
```

Local runtime ardicilligi:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose up -d --build
docker compose ps
```

Compose faylinin sintaksisini yoxlamaq ucun:

```powershell
docker compose config --quiet
```

## Local Service Ports

| Service | Port |
| --- | --- |
| api-gateway | `8080` |
| iam-service | `8081` |
| user-profile-service | `8082` |
| policy-service | `8083` |
| claim-service | `8084` |
| health-record-service | `8085` |
| ai-risk-service | `8086` |
| fraud-detection-service | `8087` |
| notification-service | `8088` |
| payment-service | `8089` |
| discovery-server | `8761` |
| config-server | `8888` |
| PostgreSQL | `5432` |
| Redis | `6379` |
| Kafka | `9092` |
| MinIO API | `9000` |
| MinIO Console | `9001` |

## IAM Login With Email Or Phone

Register zamani email mecburidir, `phoneNumber` optional-dir:

```json
{
  "email": "user@gmail.com",
  "phoneNumber": "+994501234567",
  "password": "Password123"
}
```

Email ile login:

```http
POST /api/v1/iam/login/email
```

```json
{
  "email": "user@gmail.com",
  "password": "Password123"
}
```

Telefon nomresi ile login:

```http
POST /api/v1/iam/login/phone
```

```json
{
  "phoneNumber": "+994501234567",
  "password": "Password123"
}
```

Compatibility ucun `POST /api/v1/iam/login` endpoint-i de qalir ve
`identifier` qebul edir. Yeni kodda email ve phone login ayri endpoint-lerle
istifade olunmalidir.
