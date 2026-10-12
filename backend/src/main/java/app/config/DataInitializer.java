package app.config;

import app.model.*;
import app.repository.*;
import app.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CategoryRepository categoryRepository;
    private final ScoringRuleRepository scoringRuleRepository;
    private final AchievementRepository achievementRepository;
    private final VerificationRepository verificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentStorageService documentStorageService;

    @Value("${app.upload.dir:./file-vault}")
    private String uploadDir;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            log.info("Database already initialized.");
            return;
        }

        log.info("Initializing Student Achievement Portal with comprehensive seed data...");

        // 1. Seed Categories
        Category catHackathon = categoryRepository.save(Category.builder()
                .code("HACKATHON")
                .name("Hackathon")
                .description("Team or solo development sprints, prototype builds, and hackathons")
                .defaultPoints(20)
                .iconName("code")
                .schemaJson("{\"fields\": [\"teamSize\", \"roleInTeam\", \"outcome\", \"projectGithubUrl\"]}")
                .active(true)
                .build());

        Category catSports = categoryRepository.save(Category.builder()
                .code("SPORTS")
                .name("Sports Achievement")
                .description("Athletic competitions, university tournaments, and sports meets")
                .defaultPoints(12)
                .iconName("award")
                .schemaJson("{\"fields\": [\"sportName\", \"eventType\", \"positionAwarded\"]}")
                .active(true)
                .build());

        Category catPublication = categoryRepository.save(Category.builder()
                .code("PUBLICATION")
                .name("Paper Publication")
                .description("Indexed research papers, journal articles, conferences, or patents")
                .defaultPoints(25)
                .iconName("file-text")
                .schemaJson("{\"fields\": [\"publicationType\", \"doiNumber\", \"issnIsbn\", \"venueName\", \"authorPosition\"]}")
                .active(true)
                .build());

        Category catInternship = categoryRepository.save(Category.builder()
                .code("INTERNSHIP")
                .name("Internship")
                .description("Industry internships, research apprenticeships, and summer training")
                .defaultPoints(15)
                .iconName("briefcase")
                .schemaJson("{\"fields\": [\"companyName\", \"durationWeeks\", \"stipendMonthly\", \"mode\", \"roleTitle\"]}")
                .active(true)
                .build());

        Category catCompetition = categoryRepository.save(Category.builder()
                .code("COMPETITION")
                .name("Competition")
                .description("Coding contests, quizzes, technical symposiums, and cultural fests")
                .defaultPoints(12)
                .iconName("target")
                .schemaJson("{\"fields\": [\"competitionType\", \"rankSecured\", \"prizeDetails\"]}")
                .active(true)
                .build());

        Category catCertificate = categoryRepository.save(Category.builder()
                .code("CERTIFICATE")
                .name("Certificate & Course")
                .description("Professional industry certifications, MOOC courses, and workshops")
                .defaultPoints(3)
                .iconName("check-circle")
                .schemaJson("{\"fields\": [\"issuingOrganization\", \"credentialId\", \"courseHours\"]}")
                .active(true)
                .build());

        // 2. Seed Scoring Rules (Section 13)
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("PUBLICATION").level(AchievementLevel.INTERNATIONAL).subType("JOURNAL").points(25).description("International Journal publication").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("PUBLICATION").level(AchievementLevel.NATIONAL).subType("CONFERENCE").points(15).description("National/IEEE Conference publication").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("HACKATHON").level(AchievementLevel.NATIONAL).subType("WINNER").points(20).description("National Hackathon 1st Prize").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("HACKATHON").level(AchievementLevel.NATIONAL).subType("FINALIST").points(12).description("National Hackathon Finalist").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("HACKATHON").level(AchievementLevel.STATE).subType("PARTICIPANT").points(8).description("State/College Hackathon").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("INTERNSHIP").level(AchievementLevel.NATIONAL).subType("COMPLETED").points(15).description("Completed Internship (8+ weeks)").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("SPORTS").level(AchievementLevel.NATIONAL).subType("GOLD").points(20).description("National Level Sports Medal").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("SPORTS").level(AchievementLevel.STATE).subType("SILVER").points(12).description("State Level Sports Medal").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("SPORTS").level(AchievementLevel.INTER_COLLEGE).subType("GOLD").points(6).description("Inter-College Sports Tournament").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("COMPETITION").level(AchievementLevel.INTERNATIONAL).subType("WINNER").points(18).description("Global Contest Winner").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("COMPETITION").level(AchievementLevel.NATIONAL).subType("WINNER").points(12).description("National Contest Winner").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("COMPETITION").level(AchievementLevel.STATE).subType("RUNNER").points(6).description("State Contest Prize").build());
        scoringRuleRepository.save(ScoringRule.builder().categoryCode("CERTIFICATE").level(AchievementLevel.INTRA_COLLEGE).subType("COMPLETION").points(3).description("Course or workshop certification").build());

        // 3. Seed Users & Profiles
        // Admin
        User adminUser = userRepository.save(User.builder()
                .email("admin@anurag.edu.in")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .phone("+91 9988776655")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        // Faculty Verifiers
        User cseFacultyUser = userRepository.save(User.builder()
                .email("cse.faculty@anurag.edu.in")
                .password(passwordEncoder.encode("faculty123"))
                .fullName("Dr. K. Srinivas Rao")
                .phone("+91 9848012345")
                .role(Role.ROLE_FACULTY)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Faculty cseFaculty = facultyRepository.save(Faculty.builder()
                .user(cseFacultyUser)
                .employeeId("FAC-CSE-102")
                .department("Computer Science & Engineering")
                .designation("Associate Professor & Academic Verifier")
                .assignedCategoryScope("ALL")
                .build());

        User sportsFacultyUser = userRepository.save(User.builder()
                .email("sports@anurag.edu.in")
                .password(passwordEncoder.encode("sports123"))
                .fullName("Prof. P. Ramana")
                .phone("+91 9848023456")
                .role(Role.ROLE_FACULTY)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Faculty sportsFaculty = facultyRepository.save(Faculty.builder()
                .user(sportsFacultyUser)
                .employeeId("FAC-PE-045")
                .department("Physical Education")
                .designation("Director of Physical Education")
                .assignedCategoryScope("SPORTS")
                .build());

        User placementFacultyUser = userRepository.save(User.builder()
                .email("placements@anurag.edu.in")
                .password(passwordEncoder.encode("place123"))
                .fullName("Ms. Shalini Gupta")
                .phone("+91 9848034567")
                .role(Role.ROLE_FACULTY)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Faculty placementFaculty = facultyRepository.save(Faculty.builder()
                .user(placementFacultyUser)
                .employeeId("FAC-TPO-018")
                .department("Placement Cell")
                .designation("Senior Placement Officer")
                .assignedCategoryScope("INTERNSHIP")
                .build());

        // Student 1: Sample demo account
        User demoStudentUser = userRepository.save(User.builder()
                .email("student@example.com")
                .password(passwordEncoder.encode("student123"))
                .fullName("Demo Student")
                .role(Role.ROLE_STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Student demoStudent = studentRepository.save(Student.builder()
                .user(demoStudentUser)
                .rollNumber("DEMO001")
                .branch("Computer Science & Engineering (AIML)")
                .admissionYear(2024)
                .currentSemester(3)
                .section("R")
                .profileVisibility(ProfileVisibility.PUBLIC)
                .bio("Demo student profile for previewing the Student Achievement Portal.")
                .slug("DEMO001")
                .totalPoints(0)
                .build());

        // Student 2: Peer student
        User rohanUser = userRepository.save(User.builder()
                .email("24eg105r12@anurag.edu.in")
                .password(passwordEncoder.encode("student123"))
                .fullName("Rohan Sharma")
                .phone("+91 9123456789")
                .role(Role.ROLE_STUDENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build());

        Student rohan = studentRepository.save(Student.builder()
                .user(rohanUser)
                .rollNumber("24EG105R12")
                .branch("Computer Science & Engineering")
                .admissionYear(2024)
                .currentSemester(3)
                .section("A")
                .profileVisibility(ProfileVisibility.PUBLIC)
                .bio("Competitive programmer and full stack web developer.")
                .slug("24EG105R12")
                .totalPoints(28)
                .build());

        // 4. Seed sample demo achievements with sample documents
        // Achievement A: Hackathon (Approved, 20 pts)
        Achievement achHackathon = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catHackathon)
                .title("1st Place - Smart India Hackathon (SIH) 2025")
                .description("Built an AI-driven healthcare triage assistant that reduces wait times in rural primary healthcare centres by 42%. Led the backend architecture and Spring Boot API.")
                .eventName("Smart India Hackathon Grand Finale")
                .organizer("Ministry of Education & AICTE")
                .eventDate(LocalDate.of(2025, 11, 20))
                .level(AchievementLevel.NATIONAL)
                .status(AchievementStatus.APPROVED)
                .pointsAwarded(20)
                .metadataJson("{\"teamSize\": \"6\", \"roleInTeam\": \"Backend Lead & ML Engineer\", \"outcome\": \"WINNER\", \"projectGithubUrl\": \"https://github.com/example/sih-triage-ai\"}")
                .submittedAt(LocalDateTime.of(2025, 11, 22, 10, 15))
                .updatedAt(LocalDateTime.of(2025, 11, 23, 14, 30))
                .build());

        createSampleDoc(achHackathon, "sih-winner-certificate.pdf", "application/pdf", "SIH 2025 First Prize Certificate", "CERTIFICATE");

        verificationRepository.save(Verification.builder()
                .achievement(achHackathon)
                .faculty(cseFaculty)
                .decision(AchievementStatus.APPROVED)
                .remarks("Verified with SIH national result portal. Outstanding achievement bringing laurels to Anurag University.")
                .reviewedAt(LocalDateTime.of(2025, 11, 23, 14, 30))
                .build());

        // Achievement B: Paper Publication (Approved, 25 pts)
        Achievement achPub = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catPublication)
                .title("Low-Latency Federated Learning on Edge Devices for Healthcare Diagnostics")
                .description("Published in IEEE Transactions on Edge Computing. Proposed a federated optimization algorithm ensuring strict patient data privacy across mobile telemetry units.")
                .eventName("IEEE International Conference on Distributed Systems & Edge AI")
                .organizer("IEEE Computer Society")
                .eventDate(LocalDate.of(2025, 8, 14))
                .level(AchievementLevel.INTERNATIONAL)
                .status(AchievementStatus.APPROVED)
                .pointsAwarded(25)
                .metadataJson("{\"publicationType\": \"JOURNAL\", \"doiNumber\": \"10.1109/TEDG.2025.3214567\", \"issnIsbn\": \"2168-2356\", \"venueName\": \"IEEE Transactions on Edge Computing\", \"authorPosition\": \"First Author\"}")
                .submittedAt(LocalDateTime.of(2025, 8, 18, 11, 0))
                .updatedAt(LocalDateTime.of(2025, 8, 20, 16, 45))
                .build());

        createSampleDoc(achPub, "ieee-paper-acceptance.pdf", "application/pdf", "IEEE Official Acceptance & DOI Record", "PROOF_DOCUMENT");

        verificationRepository.save(Verification.builder()
                .achievement(achPub)
                .faculty(cseFaculty)
                .decision(AchievementStatus.APPROVED)
                .remarks("DOI 10.1109/TEDG.2025.3214567 checked and verified. Published as First Author.")
                .reviewedAt(LocalDateTime.of(2025, 8, 20, 16, 45))
                .build());

        // Achievement C: Internship (Approved, 15 pts)
        Achievement achIntern = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catInternship)
                .title("AI/ML Research Intern at Quantix Tech Labs")
                .description("Contributed to high-throughput NLP pipeline for automated resume skill parsing and recommendation engines. Deployed Spring Boot microservices interfacing with PyTorch models.")
                .eventName("Summer Research Internship")
                .organizer("Quantix Tech Labs Pvt Ltd, Hyderabad")
                .eventDate(LocalDate.of(2025, 7, 30))
                .level(AchievementLevel.NATIONAL)
                .status(AchievementStatus.APPROVED)
                .pointsAwarded(15)
                .metadataJson("{\"companyName\": \"Quantix Tech Labs\", \"durationWeeks\": \"10\", \"stipendMonthly\": \"25000\", \"mode\": \"Hybrid\", \"roleTitle\": \"AI/ML Research Intern\"}")
                .submittedAt(LocalDateTime.of(2025, 8, 2, 9, 30))
                .updatedAt(LocalDateTime.of(2025, 8, 5, 11, 20))
                .build());

        createSampleDoc(achIntern, "quantix-internship-completion.pdf", "application/pdf", "Quantix Completion Certificate", "COMPLETION_LETTER");

        verificationRepository.save(Verification.builder()
                .achievement(achIntern)
                .faculty(placementFaculty)
                .decision(AchievementStatus.APPROVED)
                .remarks("Verified with HR verification email. 10 weeks completed with stipend.")
                .reviewedAt(LocalDateTime.of(2025, 8, 5, 11, 20))
                .build());

        // Achievement D: Sports (Approved, 6 pts)
        Achievement achSport = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catSports)
                .title("Gold Medalist - Inter-University Women's Badminton Tournament")
                .description("Represented Anurag University in the Inter-University Sports Championship Singles Badminton Tournament and bagged the Gold Medal.")
                .eventName("State Inter-University Sports Meet 2025")
                .organizer("Telangana State University Sports Board")
                .eventDate(LocalDate.of(2025, 4, 10))
                .level(AchievementLevel.INTER_COLLEGE)
                .status(AchievementStatus.APPROVED)
                .pointsAwarded(6)
                .metadataJson("{\"sportName\": \"Badminton\", \"eventType\": \"Singles\", \"positionAwarded\": \"GOLD\"}")
                .submittedAt(LocalDateTime.of(2025, 4, 12, 15, 0))
                .updatedAt(LocalDateTime.of(2025, 4, 14, 17, 30))
                .build());

        createSampleDoc(achSport, "badminton-gold-certificate.pdf", "application/pdf", "Badminton Gold Medal Certificate", "CERTIFICATE");

        verificationRepository.save(Verification.builder()
                .achievement(achSport)
                .faculty(sportsFaculty)
                .decision(AchievementStatus.APPROVED)
                .remarks("Verified official scorecard from Tournament Director. Excellent athletic representation.")
                .reviewedAt(LocalDateTime.of(2025, 4, 14, 17, 30))
                .build());

        // Achievement E: Certificate (Approved, 3 pts)
        Achievement achCert = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catCertificate)
                .title("AWS Certified Solutions Architect – Associate")
                .description("Demonstrated comprehensive knowledge in deploying robust, decoupled, and secure cloud applications on Amazon Web Services.")
                .eventName("AWS Certification Exam SAA-C03")
                .organizer("Amazon Web Services (AWS)")
                .eventDate(LocalDate.of(2025, 3, 5))
                .level(AchievementLevel.NATIONAL)
                .status(AchievementStatus.APPROVED)
                .pointsAwarded(3)
                .metadataJson("{\"issuingOrganization\": \"Amazon Web Services\", \"credentialId\": \"AWS-SAA-83920194\", \"courseHours\": \"80\"}")
                .submittedAt(LocalDateTime.of(2025, 3, 7, 18, 0))
                .updatedAt(LocalDateTime.of(2025, 3, 9, 10, 0))
                .build());

        createSampleDoc(achCert, "aws-solutions-architect-cert.pdf", "application/pdf", "AWS SAA Certificate", "CERTIFICATE");

        verificationRepository.save(Verification.builder()
                .achievement(achCert)
                .faculty(cseFaculty)
                .decision(AchievementStatus.APPROVED)
                .remarks("Credential ID AWS-SAA-83920194 verified via AWS Verification portal.")
                .reviewedAt(LocalDateTime.of(2025, 3, 9, 10, 0))
                .build());

        // Achievement F: Competition (PENDING - for live demo in verification queue!)
        Achievement achComp = achievementRepository.save(Achievement.builder()
                .student(demoStudent)
                .category(catCompetition)
                .title("Top 10 Finalist - National Algorithmic Coding Challenge 2026")
                .description("Ranked 8th among 4,500 participants across India in 4 rounds of algorithmic problem solving, dynamic programming, and system optimization.")
                .eventName("TCS CodeVita / National AlgoQuest 2026")
                .organizer("National Coding Consortium")
                .eventDate(LocalDate.of(2026, 2, 18))
                .level(AchievementLevel.NATIONAL)
                .status(AchievementStatus.PENDING)
                .pointsAwarded(0)
                .metadataJson("{\"competitionType\": \"Coding Contest\", \"rankSecured\": \"8\", \"prizeDetails\": \"Finalist Shield & Cash Award Rs. 10,000\"}")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build());

        createSampleDoc(achComp, "algoquest-rank-card.pdf", "application/pdf", "Official Rank Card & Leaderboard", "CERTIFICATE");

        // Total demo student points: 20 + 25 + 15 + 6 + 3 = 69 points
        demoStudent.setTotalPoints(69);
        studentRepository.save(demoStudent);

        // 5. Seed Notification
        notificationRepository.save(Notification.builder()
                .user(demoStudentUser)
                .title("Welcome to Student Achievement Portal!")
                .message("Your profile is active. You have 5 verified credentials and 69 points accumulated on the institutional ledger.")
                .link("/student/dashboard")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        notificationRepository.save(Notification.builder()
                .user(cseFacultyUser)
                .title("1 Pending Submission in Verification Queue")
                .message("Demo Student (DEMO001) submitted 'Top 10 Finalist - National Algorithmic Coding Challenge 2026' for review.")
                .link("/faculty/queue")
                .isRead(false)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());

        // 6. Seed Audit Logs
        auditLogRepository.save(AuditLog.builder()
                .userId(adminUser.getId())
                .userEmail(adminUser.getEmail())
                .action("SYSTEM_INIT")
                .entityType("SYSTEM")
                .entityId(1L)
                .details("Student Achievement Portal initialised with standard categories, verifiers, and scoring matrix.")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now().minusDays(30))
                .build());

        log.info("Student Achievement Portal initialized successfully!");
    }

    private void createSampleDoc(Achievement achievement, String filename, String mimeType, String displayName, String docType) {
        try {
            Path vault = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(vault);

            byte[] content = ("Official Document Proof: " + displayName + "\nIssued to Demo Student (DEMO001)\nAnurag University, Hyderabad\nChecksum Verified.").getBytes();
            String storedFileName = UUID.randomUUID().toString() + "-" + filename;
            Path target = vault.resolve(storedFileName);
            Files.write(target, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String checksum = documentStorageService.calculateSha256(new ByteArrayInputStream(content));

            Document doc = Document.builder()
                    .achievement(achievement)
                    .originalFileName(filename)
                    .storedFileName(storedFileName)
                    .fileType(mimeType)
                    .fileSize((long) content.length)
                    .checksumSha256(checksum)
                    .docType(docType != null ? docType : "CERTIFICATE")
                    .uploadedAt(LocalDateTime.now())
                    .build();

            doc = documentRepository.save(doc);
            achievement.getDocuments().add(doc);
        } catch (Exception e) {
            log.warn("Could not create seed document: {}", e.getMessage());
        }
    }
}
