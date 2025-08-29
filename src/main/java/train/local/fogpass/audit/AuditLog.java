package train.local.fogpass.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        })
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityType;

    private String entityId;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    private String ipAddress;

    private String requestId;

    private String userAgent;

    private String performedBy;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String entitySnapshot; // full JSON after operation

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String details; // diff JSON

    @CreationTimestamp
    private Instant timestamp;

    @Column(length = 64, nullable = false)
    private String hash; // current record hash (SHA-256)

    @Column(length = 64)
    private String prevHash; // previous record hash
}