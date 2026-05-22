package az.saglamol.common.security;

public final class InternalAuthHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLES = "X-User-Roles";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String AUTH_CONTEXT_SIGNATURE = "X-Auth-Context-Signature";

    private InternalAuthHeaders() {
    }
}
