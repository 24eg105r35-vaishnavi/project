package app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scoring_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String categoryCode; // HACKATHON, SPORTS, PUBLICATION, INTERNSHIP, COMPETITION, CERTIFICATE

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AchievementLevel level;

    @Column(length = 60)
    private String subType; // e.g. "WINNER", "FINALIST", "PARTICIPANT", "JOURNAL", "CONFERENCE", "PATENT"

    @Column(nullable = false)
    private Integer points;

    @Column(length = 200)
    private String description;
}
