package app.controller;

import app.model.*;
import app.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final AchievementService achievementService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();

        List<Achievement> achievements = achievementService.getStudentAchievements(student);
        long verifiedCount = achievements.stream().filter(a -> a.getStatus() == AchievementStatus.APPROVED).count();
        long pendingCount = achievements.stream().filter(a -> a.getStatus() == AchievementStatus.PENDING).count();
        long correctionCount = achievements.stream().filter(a -> a.getStatus() == AchievementStatus.RETURNED_FOR_CORRECTION).count();

        model.addAttribute("student", student);
        model.addAttribute("achievements", achievements);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("correctionCount", correctionCount);
        model.addAttribute("recentAchievements", achievements.stream().limit(5).toList());

        return "student/dashboard";
    }

    @GetMapping("/achievements")
    public String myAchievements(@RequestParam(value = "status", required = false) String statusFilter,
                                 Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();

        List<Achievement> list = achievementService.getStudentAchievements(student);
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                AchievementStatus st = AchievementStatus.valueOf(statusFilter.toUpperCase());
                list = list.stream().filter(a -> a.getStatus() == st).toList();
            } catch (Exception ignored) {}
        }

        model.addAttribute("student", student);
        model.addAttribute("achievements", list);
        model.addAttribute("selectedStatus", statusFilter);
        return "student/my-achievements";
    }

    @GetMapping("/achievements/new")
    public String newAchievementForm(Authentication authentication, Model model) {
        model.addAttribute("categories", achievementService.getActiveCategories());
        model.addAttribute("levels", AchievementLevel.values());
        return "student/achievement-form";
    }

    @PostMapping("/achievements/new")
    public String submitAchievement(@RequestParam("categoryCode") String categoryCode,
                                    @RequestParam("title") String title,
                                    @RequestParam(value = "description", required = false) String description,
                                    @RequestParam("eventName") String eventName,
                                    @RequestParam("organizer") String organizer,
                                    @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                                    @RequestParam("level") AchievementLevel level,
                                    @RequestParam(value = "proofFiles", required = false) List<MultipartFile> proofFiles,
                                    @RequestParam(value = "docTypes", required = false) List<String> docTypes,
                                    @RequestParam Map<String, String> allParams,
                                    HttpServletRequest request,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(authentication.getName()).orElseThrow();
            Student student = studentService.findByUser(user).orElseThrow();

            // Assemble category specific dynamic fields into JSON
            Map<String, String> metadata = new HashMap<>();
            allParams.forEach((key, val) -> {
                if (key.startsWith("meta_")) {
                    metadata.put(key.substring(5), val);
                }
            });
            String metadataJson = mapToJson(metadata);

            achievementService.submitAchievement(student, categoryCode, title, description, eventName, organizer,
                    eventDate, level, metadataJson, proofFiles, docTypes, request.getRemoteAddr());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Achievement submitted successfully! It has been queued for faculty verification.");
            return "redirect:/student/achievements";
        } catch (Exception e) {
            log.error("Failed to submit achievement", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Submission failed: " + e.getMessage());
            return "redirect:/student/achievements/new";
        }
    }

    @GetMapping("/achievements/{id}")
    public String viewAchievement(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();

        Achievement achievement = achievementService.getAchievementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));

        if (!achievement.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("You do not have permission to view this record.");
        }

        model.addAttribute("achievement", achievement);
        return "student/achievement-detail";
    }

    @GetMapping("/achievements/{id}/edit")
    public String editAchievementForm(@PathVariable("id") Long id, Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();

        Achievement achievement = achievementService.getAchievementById(id)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + id));

        if (!achievement.getStudent().getId().equals(student.getId())) {
            throw new SecurityException("You do not have permission to edit this record.");
        }

        model.addAttribute("achievement", achievement);
        model.addAttribute("levels", AchievementLevel.values());
        return "student/achievement-edit";
    }

    @PostMapping("/achievements/{id}/edit")
    public String resubmitAchievement(@PathVariable("id") Long id,
                                      @RequestParam("title") String title,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam("eventName") String eventName,
                                      @RequestParam("organizer") String organizer,
                                      @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                                      @RequestParam("level") AchievementLevel level,
                                      @RequestParam(value = "proofFiles", required = false) List<MultipartFile> proofFiles,
                                      @RequestParam(value = "docTypes", required = false) List<String> docTypes,
                                      @RequestParam Map<String, String> allParams,
                                      HttpServletRequest request,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName()).orElseThrow();
            Student student = studentService.findByUser(user).orElseThrow();

            Map<String, String> metadata = new HashMap<>();
            allParams.forEach((key, val) -> {
                if (key.startsWith("meta_")) {
                    metadata.put(key.substring(5), val);
                }
            });
            String metadataJson = mapToJson(metadata);

            achievementService.updateAndResubmit(id, student, title, description, eventName, organizer,
                    eventDate, level, metadataJson, proofFiles, docTypes, request.getRemoteAddr());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Achievement updated and resubmitted for verification!");
            return "redirect:/student/achievements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
            return "redirect:/student/achievements/" + id + "/edit";
        }
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();
        model.addAttribute("student", student);
        model.addAttribute("visibilities", ProfileVisibility.values());
        return "student/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(value = "bio", required = false) String bio,
                                @RequestParam("profileVisibility") ProfileVisibility profileVisibility,
                                @RequestParam(value = "currentSemester", required = false) Integer currentSemester,
                                @RequestParam(value = "section", required = false) String section,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        Student student = studentService.findByUser(user).orElseThrow();

        studentService.updateProfile(student, bio, profileVisibility, currentSemester, section);
        redirectAttributes.addFlashAttribute("successMessage", "Profile preferences updated successfully!");
        return "redirect:/student/profile";
    }

    private String mapToJson(Map<String, String> map) {
        if (map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(entry.getKey()).append("\": \"")
              .append(entry.getValue().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
