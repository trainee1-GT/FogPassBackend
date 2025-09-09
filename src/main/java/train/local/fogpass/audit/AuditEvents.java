package train.local.fogpass.audit;

import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;

public class AuditEvents {
    public static class BulkUploadCompleted extends ApplicationEvent {
        private final UUID jobId;
        private final Map<String, Object> payload;
        public BulkUploadCompleted(Object src, UUID jobId, Map<String, Object> payload) {
            super(src);
            this.jobId = jobId;
            this.payload = payload;
        }
        public UUID getJobId() { return jobId; }
        public Map<String, Object> getPayload() { return payload; }
    }
}