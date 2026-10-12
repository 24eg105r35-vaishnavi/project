package app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 30)
    private String employeeId;

    @Column(nullable = false, length = 50)
    private String department; // e.g. "CSE", "Sports / Physical Education", "Placement Cell"

    @Column(nullable = false, length = 50)
    private String designation; // e.g. "Associate Professor & Verifier"

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String assignedCategoryScope = "ALL"; // "ALL", "SPORTS", "INTERNSHIP", "PUBLICATION", "HACKATHON", etc.
}
