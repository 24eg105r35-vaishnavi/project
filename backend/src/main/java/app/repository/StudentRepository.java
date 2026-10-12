package app.repository;

import app.model.ProfileVisibility;
import app.model.Student;
import app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findBySlug(String slug);
    List<Student> findAllByOrderByTotalPointsDesc();
    List<Student> findByProfileVisibility(ProfileVisibility visibility);
    List<Student> findByBranch(String branch);
    boolean existsByRollNumber(String rollNumber);
}
