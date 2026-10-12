package app.service;

import app.model.*;
import app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final CategoryRepository categoryRepository;
    private final ScoringRuleRepository scoringRuleRepository;
    private final DocumentStorageService documentStorageService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryByCode(String code) {
        return categoryRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public Optional<Achievement> getAchievementById(Long id) {
        return achievementRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Achievement> getStudentAchievements(Student student) {
        return achievementRepository.findByStudentOrderBySubmittedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public List<Achievement> getVerifiedStudentAchievements(Student student) {
        return achievementRepository.findByStudentAndStatusOrderBySubmittedAtDesc(student, AchievementStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Achievement> getPendingAchievementsForFaculty(Faculty faculty) {
        List<Achievement> pending = achievementRepository.findByStatusOrderBySubmittedAtDesc(AchievementStatus.PENDING);
        if (faculty == null || "ALL".equalsIgnoreCase(faculty.getAssignedCategoryScope())) {
            return pending;
        }
        String scope = faculty.getAssignedCategoryScope().toUpperCase();
        return pending.stream()
                .filter(a -> a.getCategory().getCode().equalsIgnoreCase(scope) ||
                             a.getStudent().getBranch().toUpperCase().contains(scope))
                .toList();
    }

    @Transactional
    public Achievement submitAchievement(Student student, String categoryCode, String title, String description,
                                         String eventName, String organizer, LocalDate eventDate,
                                         AchievementLevel level, String metadataJson,
                                         List<MultipartFile> proofFiles, List<String> docTypes, String clientIp) throws IOException {

        Category category = categoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + categoryCode));

        Achievement achievement = Achievement.builder()
                .student(student)
                .category(category)
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .eventName(eventName.trim())
                .organizer(organizer.trim())
                .eventDate(eventDate)
                .level(level)
                .status(AchievementStatus.PENDING)
                .pointsAwarded(0)
                .metadataJson(metadataJson != null ? metadataJson : "{}")
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        achievement = achievementRepository.save(achievement);

        // Store proof files into Vault with SHA-256 checksums
        if (proofFiles != null && !proofFiles.isEmpty()) {
            for (int i = 0; i < proofFiles.size(); i++) {
                MultipartFile file = proofFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String docType = (docTypes != null && docTypes.size() > i) ? docTypes.get(i) : "CERTIFICATE";
                    Document doc = documentStorageService.storeFile(file, achievement, docType);
                    achievement.getDocuments().add(doc);
                }
            }
        }

        auditService.log(student.getUser().getId(), student.getUser().getEmail(), "SUBMIT_ACHIEVEMENT",
                "ACHIEVEMENT", achievement.getId(), "Submitted achievement: " + achievement.getTitle(), clientIp);

        // Notify faculty verifiers
        List<Faculty> verifiers = facultyRepository.findAll();
        for (Faculty f : verifiers) {
            if ("ALL".equalsIgnoreCase(f.getAssignedCategoryScope()) ||
                f.getAssignedCategoryScope().equalsIgnoreCase(category.getCode())) {
                notificationService.notifyUser(f.getUser(),
                        "New Verification Required: " + achievement.getTitle(),
                        student.getUser().getFullName() + " (" + student.getRollNumber() + ") submitted a new " + category.getName() + " record.",
                        "/faculty/queue");
            }
        }

        return achievement;
    }

    @Transactional
    public Achievement updateAndResubmit(Long achievementId, Student student, String title, String description,
                                         String eventName, String organizer, LocalDate eventDate,
                                         AchievementLevel level, String metadataJson,
                                         List<MultipartFile> newProofFiles, List<String> docTypes, String clientIp) throws IOException {

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        if (!achievement.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("Unauthorized: Cannot modify someone else's achievement");
        }

        achievement.setTitle(title.trim());
        achievement.setDescription(description != null ? description.trim() : "");
        achievement.setEventName(eventName.trim());
        achievement.setOrganizer(organizer.trim());
        achievement.setEventDate(eventDate);
        achievement.setLevel(level);
        achievement.setMetadataJson(metadataJson != null ? metadataJson : achievement.getMetadataJson());
        achievement.setStatus(AchievementStatus.PENDING);
        achievement.setUpdatedAt(LocalDateTime.now());

        if (newProofFiles != null) {
            for (int i = 0; i < newProofFiles.size(); i++) {
                MultipartFile file = newProofFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String docType = (docTypes != null && docTypes.size() > i) ? docTypes.get(i) : "ADDITIONAL_PROOF";
                    Document doc = documentStorageService.storeFile(file, achievement, docType);
                    achievement.getDocuments().add(doc);
                }
            }
        }

        achievement = achievementRepository.save(achievement);

        auditService.log(student.getUser().getId(), student.getUser().getEmail(), "RESUBMIT_ACHIEVEMENT",
                "ACHIEVEMENT", achievement.getId(), "Resubmitted corrected achievement: " + achievement.getTitle(), clientIp);

        return achievement;
    }

    @Transactional(readOnly = true)
    public Page<Achievement> searchAchievements(AchievementStatus status, String categoryCode,
                                                AchievementLevel level, String branch, String query, Pageable pageable) {
        String cat = (categoryCode != null && !categoryCode.isBlank()) ? categoryCode : null;
        String br = (branch != null && !branch.isBlank()) ? branch : null;
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        return achievementRepository.searchAchievements(status, cat, level, br, q, pageable);
    }

    @Transactional(readOnly = true)
    public List<Achievement> searchAllAchievements(AchievementStatus status, String categoryCode,
                                                   AchievementLevel level, String branch, String query) {
        String cat = (categoryCode != null && !categoryCode.isBlank()) ? categoryCode : null;
        String br = (branch != null && !branch.isBlank()) ? branch : null;
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        return achievementRepository.searchAllAchievements(status, cat, level, br, q);
    }

    public int calculatePoints(Achievement achievement, String subType) {
        String catCode = achievement.getCategory().getCode();
        AchievementLevel level = achievement.getLevel();

        Optional<ScoringRule> rule = scoringRuleRepository.findByCategoryCodeAndLevelAndSubType(catCode, level, subType);
        if (rule.isPresent()) {
            return rule.get().getPoints();
        }

        // Section 13 fallback table logic:
        switch (catCode) {
            case "PUBLICATION":
                if ("PATENT".equalsIgnoreCase(subType) || "JOURNAL".equalsIgnoreCase(subType)) return 25;
                return 15; // Conference
            case "HACKATHON":
                if (level == AchievementLevel.NATIONAL || level == AchievementLevel.INTERNATIONAL) {
                    return "WINNER".equalsIgnoreCase(subType) ? 20 : 12;
                }
                return 8;
            case "INTERNSHIP":
                return 15;
            case "SPORTS":
                if (level == AchievementLevel.NATIONAL || level == AchievementLevel.INTERNATIONAL) return 20;
                if (level == AchievementLevel.STATE) return 12;
                return 6;
            case "COMPETITION":
                if (level == AchievementLevel.INTERNATIONAL) return 18;
                if (level == AchievementLevel.NATIONAL) return 12;
                return 6;
            case "CERTIFICATE":
            default:
                return achievement.getCategory().getDefaultPoints() != null ? achievement.getCategory().getDefaultPoints() : 3;
        }
    }
}
