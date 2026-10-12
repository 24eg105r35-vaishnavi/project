package app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String reportType; // e.g. "NAAC_CRITERION_5_2_3", "NBA_STUDENT_PERFORMANCE", "ANNUAL_CONSOLIDATED"

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 20)
    private String academicYear; // e.g. "2024-2025"

    @Column(nullable = false, length = 100)
    private String generatedBy;

    @Column(nullable = false)
    private Long totalRecordsIncluded;

    @Column(columnDefinition = "TEXT")
    private String summaryText;

    @Column(columnDefinition = "LONGTEXT")
    private String snapshotDataJson;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();
}
