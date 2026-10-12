package app.controller;

import app.model.AuditLog;
import app.model.Category;
import app.model.Faculty;
import app.model.ScoringRule;
import app.repository.*;
import app.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CategoryRepository categoryRepository;
    private final ScoringRuleRepository scoringRuleRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final AchievementRepository achievementRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalFaculty", facultyRepository.count());
        model.addAttribute("totalAchievements", achievementRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("recentLogs", auditLogRepository.findTop50ByOrderByTimestampDesc().stream().limit(8).toList());
        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam("code") String code,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("defaultPoints") Integer defaultPoints,
                               @RequestParam(value = "schemaJson", required = false) String schemaJson,
                               HttpServletRequest request,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findByCode(code.toUpperCase().trim())
                .orElse(Category.builder().code(code.toUpperCase().trim()).build());

        category.setName(name);
        category.setDescription(description);
        category.setDefaultPoints(defaultPoints);
        if (schemaJson != null && !schemaJson.isBlank()) {
            category.setSchemaJson(schemaJson);
        }
        categoryRepository.save(category);

        auditService.logStandalone(null, authentication.getName(), "ADMIN_UPDATE_CATEGORY", "CATEGORY",
                category.getId(), "Updated category: " + category.getCode(), request.getRemoteAddr());

        redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/scoring")
    public String scoringRules(Model model) {
        model.addAttribute("rules", scoringRuleRepository.findAll());
        model.addAttribute("categories", categoryRepository.findByActiveTrue());
        return "admin/scoring";
    }

    @PostMapping("/scoring/update")
    public String updateScoringRule(@RequestParam("ruleId") Long ruleId,
                                    @RequestParam("points") Integer points,
                                    HttpServletRequest request,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        scoringRuleRepository.findById(ruleId).ifPresent(r -> {
            r.setPoints(points);
            scoringRuleRepository.save(r);
            auditService.logStandalone(null, authentication.getName(), "ADMIN_UPDATE_SCORING", "SCORING_RULE",
                    r.getId(), "Updated scoring weight for " + r.getCategoryCode() + " to " + points + " pts", request.getRemoteAddr());
        });
        redirectAttributes.addFlashAttribute("successMessage", "Scoring weight updated successfully.");
        return "redirect:/admin/scoring";
    }

    @GetMapping("/verifiers")
    public String verifiers(Model model) {
        model.addAttribute("faculties", facultyRepository.findAll());
        model.addAttribute("categories", categoryRepository.findByActiveTrue());
        return "admin/verifiers";
    }

    @PostMapping("/verifiers/update-scope")
    public String updateVerifierScope(@RequestParam("facultyId") Long facultyId,
                                      @RequestParam("assignedCategoryScope") String assignedCategoryScope,
                                      HttpServletRequest request,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        facultyRepository.findById(facultyId).ifPresent(f -> {
            f.setAssignedCategoryScope(assignedCategoryScope);
            facultyRepository.save(f);
            auditService.logStandalone(null, authentication.getName(), "ADMIN_UPDATE_VERIFIER_SCOPE", "FACULTY",
                    f.getId(), "Mapped verifier " + f.getUser().getFullName() + " to scope " + assignedCategoryScope, request.getRemoteAddr());
        });
        redirectAttributes.addFlashAttribute("successMessage", "Verifier scope updated successfully.");
        return "redirect:/admin/verifiers";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<AuditLog> logPage = auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, 20));
        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        return "admin/audit-logs";
    }
}
