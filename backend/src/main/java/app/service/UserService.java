package app.service;

import app.model.Role;
import app.model.Student;
import app.model.User;
import app.repository.StudentRepository;
import app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Value("${app.portal.domain:@anurag.edu.in}")
    private String institutionalDomain;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Transactional
    public Student registerStudent(String email, String rawPassword, String fullName, String phone,
                                   String rollNumber, String branch, Integer admissionYear, Integer currentSemester) {
        String cleanEmail = email.toLowerCase().trim();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("An account with email " + cleanEmail + " already exists.");
        }

        if (studentRepository.existsByRollNumber(rollNumber.toUpperCase().trim())) {
            throw new IllegalArgumentException("Student with Roll Number " + rollNumber + " is already registered.");
        }

        User user = User.builder()
                .email(cleanEmail)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName.trim())
                .phone(phone != null ? phone.trim() : "")
                .role(Role.ROLE_STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        String slug = rollNumber.toUpperCase().trim().replaceAll("[^A-Z0-9]", "-");

        Student student = Student.builder()
                .user(user)
                .rollNumber(rollNumber.toUpperCase().trim())
                .branch(branch != null ? branch.trim() : "Computer Science & Engineering")
                .admissionYear(admissionYear != null ? admissionYear : 2024)
                .currentSemester(currentSemester != null ? currentSemester : 1)
                .slug(slug)
                .totalPoints(0)
                .build();
        student = studentRepository.save(student);

        auditService.logStandalone(user.getId(), user.getEmail(), "REGISTER_STUDENT", "STUDENT", student.getId(),
                "Registered new student " + student.getRollNumber(), "127.0.0.1");

        return student;
    }
}
