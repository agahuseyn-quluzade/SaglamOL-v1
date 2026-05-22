package az.saglamol.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InternalAuthFilter extends OncePerRequestFilter {

    public static final String MDC_CORRELATION_ID = "correlationId";

    private final AuthContextResolver authContextResolver;
    private final List<String> publicPathPrefixes;

    public InternalAuthFilter(AuthContextResolver authContextResolver, List<String> publicPathPrefixes) {
        this.authContextResolver = authContextResolver;
        this.publicPathPrefixes = List.copyOf(publicPathPrefixes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return publicPathPrefixes.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            AuthContext context = authContextResolver.resolve(request);
            AuthContextHolder.set(context);
            MDC.put(MDC_CORRELATION_ID, context.correlationId());
            response.setHeader(InternalAuthHeaders.CORRELATION_ID, context.correlationId());
            filterChain.doFilter(request, response);
        } catch (InternalAuthException exception) {
            writeError(response, exception);
        } finally {
            AuthContextHolder.clear();
            MDC.remove(MDC_CORRELATION_ID);
        }
    }

    private void writeError(HttpServletResponse response, InternalAuthException exception) throws IOException {
        int status = "INSUFFICIENT_ROLE".equals(exception.getErrorCode()) ? 403 : 401;
        String correlationId = MDC.get(MDC_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(InternalAuthHeaders.CORRELATION_ID, correlationId);
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"errorCode":"%s","message":"%s","correlationId":"%s"}
                """.formatted(
                Instant.now(),
                status,
                escape(exception.getErrorCode()),
                escape(exception.getMessage()),
                escape(correlationId)
        ));
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
