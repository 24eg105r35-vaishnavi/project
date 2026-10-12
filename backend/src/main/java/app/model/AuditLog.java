package app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "userId"),
    @Index(name = "idx_audit_time", columnList = "timestamp"),
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 150)
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String action; // e.g. "SUBMIT_ACHIEVEMENT", "VERIFY_APPROVED", "VERIFY_REJECTED", "UPDATE_SCORING"

    @Column(nullable = false, length = 50)
    private String entityType; // e.g. "ACHIEVEMENT", "USER", "CATEGORY", "SCORING_RULE"

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 64)
    private String ipAddress;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
