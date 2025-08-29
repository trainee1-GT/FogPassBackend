package train.local.fogpass.audit;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
public class AuditEvent {
    AuditAction action;
    String entityType;
    String entityId;
    String performedBy;
    String ipAddress;
    String requestId;
    String userAgent;
    Map<String, Object> diff; // diff map
    String snapshotJson;      // final snapshot JSON
    Set<String> maskedFields; // effective masks used
}