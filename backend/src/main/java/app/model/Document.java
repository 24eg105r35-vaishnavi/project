package app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_doc_checksum", columnList = "checksumSha256")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255)
    private String storedFileName;

    @Column(nullable = false, length = 100)
    private String fileType; // image/jpeg, image/png, application/pdf

    @Column(nullable = false)
    private Long fileSize; // in bytes

    @Column(nullable = false, length = 64)
    private String checksumSha256;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String docType = "CERTIFICATE"; // CERTIFICATE, OFFER_LETTER, COMPLETION_LETTER, PHOTO

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
