package app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_roll", columnList = "rollNumber"),
    @Index(name = "idx_student_slug", columnList = "slug"),
    @Index(name = "idx_student_branch", columnList = "branch")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 30)
    private String rollNumber;

    @Column(nullable = false, length = 50)
    private String branch; // e.g. "Computer Science & Engineering (AIML)"

    @Column(nullable = false)
    private Integer admissionYear; // e.g. 2024

    @Column(nullable = false)
    private Integer currentSemester; // e.g. 2 or 3

    @Column(length = 10)
    private String section; // e.g. "A"

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(unique = true, length = 100)
    private String slug;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalPoints = 0;
}
