package az.saglamol.claim.entity;

public enum ClaimStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    NEEDS_MORE_DOCUMENTS,
    APPROVED,
    REJECTED,
    PAYMENT_PENDING,
    PAID,
    PAYOUT_FAILED,
    CANCELLED
}
