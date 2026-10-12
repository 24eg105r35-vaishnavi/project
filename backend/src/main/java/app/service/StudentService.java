package app.service;

import app.model.Achievement;
import app.model.AchievementStatus;
import app.model.ProfileVisibility;
import app.model.Student;
import app.model.User;
import app.repository.AchievementRepository;
import app.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final AchievementRepository achievementRepository;

    @Transactional(readOnly = true)
    public Optional<Student> findByUser(User user) {
        return studentRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Student> findByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber.toUpperCase().trim());
    }

    @Transactional(readOnly = true)
    public Optional<Student> findBySlugOrRoll(String identifier) {
        if (identifier == null) return Optional.empty();
        String clean = identifier.trim().toUpperCase();
        Optional<Student> byRoll = studentRepository.findByRollNumber(clean);
        if (byRoll.isPresent()) return byRoll;
        return studentRepository.findBySlug(identifier.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<Student> getLeaderboard() {
        return studentRepository.findAllByOrderByTotalPointsDesc();
    }

    @Transactional
    public Student updateProfile(Student student, String bio, ProfileVisibility visibility, Integer currentSemester, String section) {
        if (bio != null) student.setBio(bio);
        if (visibility != null) student.setProfileVisibility(visibility);
        if (currentSemester != null) student.setCurrentSemester(currentSemester);
        if (section != null) student.setSection(section);
        return studentRepository.save(student);
    }

    @Transactional
    public void recalculatePoints(Student student) {
        List<Achievement> approved = achievementRepository.findByStudentAndStatusOrderBySubmittedAtDesc(student, AchievementStatus.APPROVED);
        int total = approved.stream().mapToInt(a -> a.getPointsAwarded() != null ? a.getPointsAwarded() : 0).sum();
        student.setTotalPoints(total);
        studentRepository.save(student);
    }
}
