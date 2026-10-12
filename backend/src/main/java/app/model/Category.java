package app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code; // CERTIFICATE, HACKATHON, SPORTS, PUBLICATION, INTERNSHIP, COMPETITION

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Integer defaultPoints = 10;

    @Column(length = 50)
    private String iconName;

    @Column(columnDefinition = "TEXT")
    private String schemaJson;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
