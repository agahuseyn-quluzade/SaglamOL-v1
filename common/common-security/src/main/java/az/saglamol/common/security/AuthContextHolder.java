package az.saglamol.common.security;

import java.util.Optional;

public final class AuthContextHolder {

    private static final ThreadLocal<AuthContext> CONTEXT = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthContext context) {
        CONTEXT.set(context);
    }

    public static AuthContext getRequired() {
        AuthContext context = CONTEXT.get();
        if (context == null) {
            throw new InternalAuthException("AUTH_CONTEXT_MISSING", "Authentication context is missing");
        }
        return context;
    }

    public static Optional<AuthContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
