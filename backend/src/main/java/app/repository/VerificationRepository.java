package app.repository;

import app.model.Achievement;
import app.model.Faculty;
import app.model.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByAchievementOrderByReviewedAtDesc(Achievement achievement);
    List<Verification> findByFacultyOrderByReviewedAtDesc(Faculty faculty);
}
