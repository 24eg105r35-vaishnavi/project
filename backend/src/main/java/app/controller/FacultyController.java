package app.controller;

import app.model.*;
import app.repository.FacultyRepository;
import app.service.AchievementService;
import app.service.UserService;
import app.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/faculty")
@RequiredArgsConstructor
@Slf4j
public class FacultyController {

    private final UserService userService;
    private final FacultyRepository facultyRepository;
    private final AchievementService achievementService;
    private final VerificationService verificationService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();

        List<Achievement> pendingQueue = achievementService.getPendingAchievementsForFaculty(faculty);

        model.addAttribute("faculty", faculty);
        model.addAttribute("pendingCount", pendingQueue.size());
        model.addAttribute("recentPending", pendingQueue.stream().limit(6).toList());

        return "faculty/dashboard";
    }

    @GetMapping("/queue")
    public String verificationQueue(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();

        List<Achievement> queue = achievementService.getPendingAchievementsForFaculty(faculty);

        model.addAttribute("faculty", faculty);
        model.addAttribute("queue", queue);
        return "faculty/verification-queue";
    }

    @GetMapping("/review/{id}")
    public String reviewAchievement(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Faculty faculty = facultyRepository.findByUser(user).orElseThrow();

        Achievement achievement = achievementService.getAchievementById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Achievement not found"));

        model.addAttribute("faculty", faculty);
        model.addAttribute("achievement", achievement);
        return "faculty/review-detail";
    }

    @PostMapping("/verify/{id}")
    public String executeVerification(@PathVariable("id") Long id,
                                      @RequestParam("decision") AchievementStatus decision,
                                      @RequestParam(value = "remarks", required = false) String remarks,
                                      @RequestParam(value = "subType", required = false) String subType,
                                      HttpServletRequest request,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElseThrow();
            Faculty faculty = facultyRepository.findByUser(user).orElseThrow();

            verificationService.verifyAchievement(id, faculty, decision, remarks, subType, request.getRemoteAddr());

            String actionText = decision == AchievementStatus.APPROVED ? "approved" :
                                decision == AchievementStatus.RETURNED_FOR_CORRECTION ? "returned for student correction" : "rejected";

            redirectAttributes.addFlashAttribute("successMessage",
                    "Achievement successfully " + actionText + " with permanent audit record.");
            return "redirect:/faculty/queue";
        } catch (Exception e) {
            log.error("Verification failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Verification error: " + e.getMessage());
            return "redirect:/faculty/review/" + id;
        }
    }
}
