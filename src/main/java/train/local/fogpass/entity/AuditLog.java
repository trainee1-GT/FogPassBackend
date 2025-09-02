package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_actor", columnList = "actor")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., FILE_UPLOAD, FILE_DELETE

    @Column
    private String actor; // username or system

    @Column(length = 1024)
    private String details; // free-form JSON string or message

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AuditLog(String action, String actor, String details) {
        this.action = action;
        this.actor = actor;
        this.details = details;
    }
}