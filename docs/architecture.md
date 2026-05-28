# SaglamOL Architecture

## C4 Context

```mermaid
C4Context
    title SaglamOL Context
    Person(patient, "Patient", "Buys policies, uploads documents, submits claims")
    Person(agent, "Insurance Agent", "Reviews company claims")
    Person(hospitalStaff, "Hospital Staff", "Creates records and documents")
    Person(admin, "Admin", "Global platform operator")
    System(saglamol, "SaglamOL Platform", "Multi-company e-health insurance backend")
    System_Ext(ai, "External AI Provider", "Risk model API")
    System_Ext(minio, "MinIO", "Medical document object storage")
    Rel(patient, saglamol, "Uses REST APIs")
    Rel(agent, saglamol, "Reviews claims")
    Rel(hospitalStaff, saglamol, "Manages health records")
    Rel(admin, saglamol, "Operates platform")
    Rel(saglamol, ai, "Risk prompts")
    Rel(saglamol, minio, "Presigned upload/download")
```

## C4 Container

```mermaid
C4Container
    title SaglamOL Containers
    Person(user, "User")
    Container(gateway, "API Gateway", "Spring Cloud Gateway", "JWT verification and header propagation")
    Container(iam, "IAM Service", "Spring Boot", "Users, roles, tokens")
    Container(profile, "User Profile Service", "Spring Boot", "Companies, hospitals, profiles")
    Container(policy, "Policy Service", "Spring Boot", "Products, policies, eligibility")
    Container(claim, "Claim Service", "Spring Boot", "Claims and review lifecycle")
    Container(health, "Health Record Service", "Spring Boot", "Records, treatments, documents")
    Container(ai, "AI Risk Service", "Spring Boot", "External AI risk assessment")
    Container(fraud, "Fraud Detection Service", "Spring Boot", "Rule-based fraud checks")
    Container(payment, "Payment Service", "Spring Boot", "Premium and payout payments")
    Container(notification, "Notification Service", "Spring Boot", "Templates and mock senders")
    ContainerDb(postgres, "PostgreSQL", "Database", "Service-owned databases")
    ContainerQueue(kafka, "Kafka", "Event broker", "Domain events and DLT")
    ContainerDb(redis, "Redis", "Cache", "Policy reservation support")
    Rel(user, gateway, "HTTPS/REST")
    Rel(gateway, iam, "REST")
    Rel(gateway, profile, "REST")
    Rel(gateway, policy, "REST")
    Rel(gateway, claim, "REST")
    Rel(gateway, health, "REST")
    Rel(claim, kafka, "Outbox publish")
    Rel(ai, kafka, "Consume/publish")
    Rel(fraud, kafka, "Consume/publish")
    Rel(payment, kafka, "Consume/publish")
    Rel(notification, kafka, "Consume")
    Rel(iam, postgres, "JPA")
    Rel(profile, postgres, "JPA")
    Rel(policy, postgres, "JPA")
    Rel(claim, postgres, "JPA")
    Rel(policy, redis, "Reservations")
```

## Login Sequence

```mermaid
sequenceDiagram
    participant U as User
    participant G as API Gateway
    participant I as IAM Service
    U->>G: POST /auth/login
    G->>I: Forward login
    I->>I: Validate bcrypt password
    I-->>G: Access token + refresh token
    G-->>U: Token response
    U->>G: API request with Bearer token
    G->>G: Validate JWT
    G->>G: Add X-User-Id, X-Roles, X-Correlation-Id
```

## Company Onboarding

```mermaid
sequenceDiagram
    participant A as Admin
    participant P as User Profile
    participant K as Kafka
    participant N as Notification
    A->>P: Create insurance company
    P->>P: Save ACTIVE/DRAFT company
    P->>K: InsuranceCompanyCreatedEvent
    N->>K: Consume event idempotently
    N->>N: Create notification from template
```

## Hospital Onboarding

```mermaid
sequenceDiagram
    participant A as Admin
    participant P as User Profile
    A->>P: Create hospital
    A->>P: Create branch
    A->>P: Create hospital staff profile
    A->>P: Assign doctor to hospital/branch
```

## Policy Purchase

```mermaid
sequenceDiagram
    participant Patient
    participant Policy
    participant Payment
    participant Kafka
    Patient->>Policy: Issue policy
    Policy->>Policy: Validate ACTIVE product
    Policy->>Kafka: PolicyCreatedEvent
    Patient->>Payment: Pay premium
    Payment->>Kafka: PaymentCompletedEvent
    Policy->>Kafka: Consume payment event
    Policy->>Policy: Activate policy
```

## Claim Submit

```mermaid
sequenceDiagram
    participant Patient
    participant Claim
    participant Policy
    participant Kafka
    Patient->>Claim: Create claim + items + documents
    Patient->>Claim: Submit claim
    Claim->>Policy: Policy detail + eligibility
    Claim->>Policy: Reserve limit
    Claim->>Claim: SUBMITTED + history
    Claim->>Kafka: ClaimSubmittedEvent
    Claim-->>Patient: Response without waiting for AI/Fraud
```

## AI And Fraud Async

```mermaid
sequenceDiagram
    participant Kafka
    participant AI as AI Risk
    participant Fraud
    participant Health
    participant Claim
    Kafka-->>AI: ClaimSubmittedEvent
    AI->>AI: Build PII-minimized prompt
    AI->>Kafka: RiskAnalysisCompletedEvent
    Kafka-->>Fraud: ClaimSubmittedEvent
    Fraud->>Health: Batch document hash lookup
    Fraud->>Fraud: Rule scoring
    Fraud->>Kafka: FraudCheckCompletedEvent
    Kafka-->>Claim: Risk/Fraud completed events
    Claim->>Claim: Update risk and fraud fields
```

## Claim Approval, Payment And Notification

```mermaid
sequenceDiagram
    participant Agent
    participant Claim
    participant Policy
    participant Kafka
    participant Payment
    participant Notification
    Agent->>Claim: Approve claim
    Claim->>Policy: Commit reservation
    Claim->>Claim: APPROVED + decision
    Claim->>Kafka: ClaimApprovedEvent
    Kafka-->>Payment: ClaimApprovedEvent
    Payment->>Payment: Create payout once
    Payment->>Kafka: ClaimPayoutCompletedEvent
    Kafka-->>Claim: Payout completed
    Claim->>Claim: PAID
    Kafka-->>Notification: Claim/payment events
    Notification->>Notification: Create/send mock notification
```
