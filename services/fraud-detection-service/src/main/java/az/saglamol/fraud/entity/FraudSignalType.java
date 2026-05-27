package az.saglamol.fraud.entity;

public enum FraudSignalType {
    DUPLICATE_DOCUMENT,
    FREQUENT_CLAIMS,
    HIGH_AMOUNT,
    SUSPICIOUS_TIMING,
    HOSPITAL_ANOMALY,
    DOCTOR_ANOMALY,
    WAITING_PERIOD_VIOLATION
}
