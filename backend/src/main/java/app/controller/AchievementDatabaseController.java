package app.controller;

import app.model.Achievement;
import app.model.AchievementLevel;
import app.model.AchievementStatus;
import app.service.AchievementService;
import app.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AchievementDatabaseController {

    private final AchievementService achievementService;
    private final ExportService exportService;

    @GetMapping("/explore")
    public String browseAchievements(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) AchievementLevel level,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 15;
        // Centralised database returns verified records by default
        AchievementStatus status = AchievementStatus.APPROVED;

        Page<Achievement> results = achievementService.searchAchievements(
                status, category, level, branch, query, PageRequest.of(page, pageSize));

        model.addAttribute("achievements", results.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", results.getTotalPages());
        model.addAttribute("totalElements", results.getTotalElements());
        model.addAttribute("categories", achievementService.getActiveCategories());
        model.addAttribute("levels", AchievementLevel.values());

        // Echo back search parameters
        model.addAttribute("selectedQuery", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedBranch", branch);

        return "explore/database";
    }

    @GetMapping("/explore/export/csv")
    public void exportAchievementsCsv(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) AchievementLevel level,
            @RequestParam(value = "branch", required = false) String branch,
            HttpServletResponse response) throws IOException {

        AchievementStatus status = AchievementStatus.APPROVED;
        List<Achievement> list = achievementService.searchAllAchievements(status, category, level, branch, query);

        String csvData = exportService.generateCsv(list);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"anurag_achievements_export.csv\"");
        response.getWriter().write(csvData);
        response.getWriter().flush();
    }
}
