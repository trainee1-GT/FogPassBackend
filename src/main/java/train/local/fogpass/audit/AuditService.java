package train.local.fogpass.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("auditTaskExecutor")
    public void handleAuditEvent(AuditEvent event) {
        try {
            String prevHash = auditLogRepository.findTopByOrderByIdDesc()
                    .map(AuditLog::getHash)
                    .orElse(null);

            AuditLog logEntry = new AuditLog();
            logEntry.setAction(event.getAction());
            logEntry.setEntityType(event.getEntityType());
            logEntry.setEntityId(event.getEntityId());
            logEntry.setIpAddress(event.getIpAddress());
            logEntry.setRequestId(event.getRequestId());
            logEntry.setUserAgent(event.getUserAgent());
            logEntry.setPerformedBy(event.getPerformedBy());
            logEntry.setEntitySnapshot(event.getSnapshotJson());
            logEntry.setDetails(event.getDiff() != null ? toJson(event.getDiff()) : null);
            logEntry.setPrevHash(prevHash);

            // compute hash = SHA-256(prevHash + currentRecordJson)
            String recordJson = toJson(logEntry);
            String hash = sha256((prevHash == null ? "" : prevHash) + recordJson);
            logEntry.setHash(hash);

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}