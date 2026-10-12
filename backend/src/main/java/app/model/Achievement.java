package app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements", indexes = {
    @Index(name = "idx_achieve_student", columnList = "student_id"),
    @Index(name = "idx_achieve_cat", columnList = "category_id"),
    @Index(name = "idx_achieve_status", columnList = "status"),
    @Index(name = "idx_achieve_level", columnList = "level"),
    @Index(name = "idx_achieve_date", columnList = "eventDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 150)
    private String eventName;

    @Column(nullable = false, length = 150)
    private String organizer;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AchievementLevel level;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private AchievementStatus status = AchievementStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private Integer pointsAwarded = 0;

    @Column(columnDefinition = "TEXT")
    private String metadataJson; // Category specific fields stored cleanly as JSON

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("reviewedAt DESC")
    private List<Verification> verifications = new ArrayList<>();
}
