package train.local.fogpass.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditRetentionService {

    private final AuditProperties auditProperties;

    @PersistenceContext
    private EntityManager entityManager;

    // Daily at 02:30
    @Scheduled(cron = "0 30 2 * * *")
    public void enforceRetention() {
        int days = auditProperties.getRetention().getDays();
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        try {
            int deleted = entityManager.createQuery("DELETE FROM AuditLog a WHERE a.timestamp < :cutoff")
                    .setParameter("cutoff", cutoff)
                    .executeUpdate();
            if (deleted > 0) {
                log.info("Audit retention enforced. Deleted {} records older than {} days.", deleted, days);
            }
        } catch (Exception e) {
            log.error("Failed to enforce audit retention: {}", e.getMessage());
        }
    }
}