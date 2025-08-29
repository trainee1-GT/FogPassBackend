package train.local.fogpass.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuditContext {
    private final String username;
    private final String ipAddress;
    private final String requestId;
    private final String userAgent;

    public AuditContext(HttpServletRequest request) {
        this.username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "system";
        this.ipAddress = request.getRemoteAddr();
        this.requestId = defaultIfBlank(request.getHeader("X-Request-Id"), java.util.UUID.randomUUID().toString());
        this.userAgent = defaultIfBlank(request.getHeader("User-Agent"), "unknown");
    }

    private static String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    public String getUsername() { return username; }
    public String getIpAddress() { return ipAddress; }
    public String getRequestId() { return requestId; }
    public String getUserAgent() { return userAgent; }
}