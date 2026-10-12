package app.repository;

import app.model.Achievement;
import app.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByChecksumSha256(String checksumSha256);
    List<Document> findByAchievement(Achievement achievement);
}
