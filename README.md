# SaglamOL E-Health Insurance Backend

Gradle multi-module Spring Boot microservice backend for an e-health insurance platform.

## Local Runtime

Build and start the full Docker runtime:

```powershell
.\gradlew.bat clean build
.\gradlew.bat bootJar
docker compose up -d --build
docker compose ps
```

Follow logs for one service:

```powershell
docker compose logs -f <service-name>
```

Example:

```powershell
docker compose logs -f iam-service
```

Stop the stack:

```powershell
docker compose down
```

Reset local volumes:

```powershell
docker compose down -v
```

## Main Ports

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
