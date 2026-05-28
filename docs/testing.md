# SaglamOL Testing

## Unit Tests

Unit tests use JUnit 5 and Mockito. Repositories and external clients are mocked; no real database is required.

```powershell
.\gradlew.bat test
```

Run focused unit suites:

```powershell
.\gradlew.bat :common:common-security:test :common:common-kafka:test
.\gradlew.bat :services:user-profile-service:test :services:policy-service:test
.\gradlew.bat :services:claim-service:test :services:payment-service:test
.\gradlew.bat :services:fraud-detection-service:test :services:ai-risk-service:test :services:notification-service:test
```

## Integration Tests

Integration tests use `@SpringBootTest` with Testcontainers:

- IAM auth flow: PostgreSQL container.
- Policy lifecycle flow: PostgreSQL and Redis containers.
- Claim outbox flow: PostgreSQL and Kafka containers.

Docker must be installed and running. Tests are annotated with `@Testcontainers(disabledWithoutDocker = true)`, so environments without Docker skip container-backed tests instead of failing during normal unit test runs.

Run integration-focused modules:

```powershell
.\gradlew.bat :services:iam-service:test :services:policy-service:test :services:claim-service:test
```

## Controller Tests

Controller tests use MockMvc or lightweight controller tests for request/response behavior:

- success path
- validation failures
- unauthorized/forbidden access
- not found
- invalid state or conflict

The current coverage is strongest for internal policy/profile endpoints and business service state transitions. Public controller matrix coverage should continue expanding as endpoint contracts stabilize.

## Docker Smoke Test

The smoke script builds the project, validates Docker Compose, starts the local stack and checks every service health endpoint.

```powershell
.\scripts\docker-smoke-test.ps1
```

The script runs:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose config --quiet
docker compose up -d --build
docker compose ps
```

Then it checks:

- `http://localhost:8080/actuator/health`
- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8083/actuator/health`
- `http://localhost:8084/actuator/health`
- `http://localhost:8085/actuator/health`
- `http://localhost:8086/actuator/health`
- `http://localhost:8087/actuator/health`
- `http://localhost:8088/actuator/health`
- `http://localhost:8089/actuator/health`
- `http://localhost:8761/actuator/health`
- `http://localhost:8888/actuator/health`

## Requirements

- Java 21
- Docker Desktop or Docker Engine
- Gradle wrapper from the repository
- Network access for pulling Testcontainers and Docker images

## Remaining Weak Areas

- Full MockMvc matrix is not yet exhaustive for every public endpoint in every service.
- Cross-service E2E through API Gateway should be expanded after stable seeded demo credentials are available.
- Kafka consumer retry/DLT behavior has focused unit coverage, but full broker-level DLT integration should be broadened.
