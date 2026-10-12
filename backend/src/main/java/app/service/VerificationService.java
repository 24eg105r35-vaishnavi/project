package app.service;

import app.model.*;
import app.repository.AchievementRepository;
import app.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    private final AchievementRepository achievementRepository;
    private final VerificationRepository verificationRepository;
    private final StudentService studentService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final AchievementService achievementService;

    @Transactional
    public Verification verifyAchievement(Long achievementId, Faculty faculty, AchievementStatus decision,
                                          String remarks, String subType, String clientIp) {

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        if (decision == AchievementStatus.PENDING) {
            throw new IllegalArgumentException("Decision cannot be PENDING");
        }

        if ((decision == AchievementStatus.REJECTED || decision == AchievementStatus.RETURNED_FOR_CORRECTION) &&
            (remarks == null || remarks.trim().isBlank())) {
            throw new IllegalArgumentException("A remark/reason is mandatory when rejecting or requesting correction.");
        }

        // 1. Append to VERIFICATION table (permanent record, append-only)
        Verification verification = Verification.builder()
                .achievement(achievement)
                .faculty(faculty)
                .decision(decision)
                .remarks(remarks != null ? remarks.trim() : "")
                .reviewedAt(LocalDateTime.now())
                .build();
        verification = verificationRepository.save(verification);

        // 2. Update achievement status and points
        achievement.setStatus(decision);
        achievement.setUpdatedAt(LocalDateTime.now());

        if (decision == AchievementStatus.APPROVED) {
            int points = achievementService.calculatePoints(achievement, subType);
            achievement.setPointsAwarded(points);
        } else {
            achievement.setPointsAwarded(0);
        }

        achievementRepository.save(achievement);

        // 3. Recalculate student total score
        studentService.recalculatePoints(achievement.getStudent());

        // 4. Log Audit entry in same transaction
        auditService.log(faculty.getUser().getId(), faculty.getUser().getEmail(),
                "VERIFICATION_" + decision.name(), "ACHIEVEMENT", achievement.getId(),
                "Faculty " + faculty.getUser().getFullName() + " marked achievement as " + decision.name() +
                (remarks != null && !remarks.isBlank() ? " with reason: " + remarks : ""), clientIp);

        // 5. Send Notification to Student
        String title;
        String message;
        String link = "/student/achievements";

        switch (decision) {
            case APPROVED:
                title = "Achievement Verified! +" + achievement.getPointsAwarded() + " Points";
                message = "Your submission '" + achievement.getTitle() + "' has been approved by Prof. " +
                          faculty.getUser().getFullName() + ".";
                break;
            case RETURNED_FOR_CORRECTION:
                title = "Correction Requested: " + achievement.getTitle();
                message = "Faculty remarked: \"" + remarks + "\". Please update your details/proof and resubmit.";
                break;
            case REJECTED:
            default:
                title = "Achievement Not Approved: " + achievement.getTitle();
                message = "Faculty remarked: \"" + remarks + "\".";
                break;
        }

        notificationService.notifyUser(achievement.getStudent().getUser(), title, message, link);

        return verification;
    }
}
