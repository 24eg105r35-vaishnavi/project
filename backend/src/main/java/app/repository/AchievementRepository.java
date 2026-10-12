package app.repository;

import app.model.Achievement;
import app.model.AchievementLevel;
import app.model.AchievementStatus;
import app.model.Category;
import app.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByStudentOrderBySubmittedAtDesc(Student student);

    List<Achievement> findByStudentAndStatusOrderBySubmittedAtDesc(Student student, AchievementStatus status);

    List<Achievement> findByStatusOrderBySubmittedAtDesc(AchievementStatus status);

    long countByStatus(AchievementStatus status);

    long countByStudentAndStatus(Student student, AchievementStatus status);

    // Advanced search for Centralised Achievement Database (Feature 10)
    @Query("SELECT a FROM Achievement a " +
           "JOIN a.student s " +
           "JOIN s.user u " +
           "JOIN a.category c " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:categoryCode IS NULL OR c.code = :categoryCode) " +
           "AND (:level IS NULL OR a.level = :level) " +
           "AND (:branch IS NULL OR LOWER(s.branch) LIKE LOWER(CONCAT('%', :branch, '%'))) " +
           "AND (:query IS NULL OR " +
           "     LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(a.eventName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY a.submittedAt DESC")
    Page<Achievement> searchAchievements(
            @Param("status") AchievementStatus status,
            @Param("categoryCode") String categoryCode,
            @Param("level") AchievementLevel level,
            @Param("branch") String branch,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT a FROM Achievement a " +
           "JOIN a.student s " +
           "JOIN s.user u " +
           "JOIN a.category c " +
           "WHERE (:status IS NULL OR a.status = :status) " +
           "AND (:categoryCode IS NULL OR c.code = :categoryCode) " +
           "AND (:level IS NULL OR a.level = :level) " +
           "AND (:branch IS NULL OR LOWER(s.branch) LIKE LOWER(CONCAT('%', :branch, '%'))) " +
           "AND (:query IS NULL OR " +
           "     LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(a.eventName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY a.submittedAt DESC")
    List<Achievement> searchAllAchievements(
            @Param("status") AchievementStatus status,
            @Param("categoryCode") String categoryCode,
            @Param("level") AchievementLevel level,
            @Param("branch") String branch,
            @Param("query") String query
    );

    @Query("SELECT a FROM Achievement a WHERE a.status = 'APPROVED' ORDER BY a.eventDate DESC")
    List<Achievement> findAllApproved();

    @Query("SELECT a.category.name, COUNT(a) FROM Achievement a WHERE a.status = 'APPROVED' GROUP BY a.category.name")
    List<Object[]> countApprovedByCategory();

    @Query("SELECT a.level, COUNT(a) FROM Achievement a WHERE a.status = 'APPROVED' GROUP BY a.level")
    List<Object[]> countApprovedByLevel();

    @Query("SELECT a.student.branch, COUNT(a) FROM Achievement a WHERE a.status = 'APPROVED' GROUP BY a.student.branch")
    List<Object[]> countApprovedByBranch();
}
