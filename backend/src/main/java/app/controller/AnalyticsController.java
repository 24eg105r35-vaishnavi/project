package app.controller;

import app.model.ReportSnapshot;
import app.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public String analyticsDashboard(Model model) {
        Map<String, Object> stats = analyticsService.getOverallStats();
        Map<String, Long> categoryData = analyticsService.getCategoryDistribution();
        Map<String, Long> levelData = analyticsService.getLevelDistribution();
        Map<String, Long> branchData = analyticsService.getBranchDistribution();

        // Render zero-payload, ultrafast server-side SVG charts
        String categorySvg = analyticsService.generateBarChartSvg(categoryData, "#6366f1");
        String levelSvg = analyticsService.generateBarChartSvg(levelData, "#0ea5e9");
        String branchSvg = analyticsService.generateBarChartSvg(branchData, "#10b981");

        List<ReportSnapshot> snapshots = analyticsService.getAllSnapshots();

        model.addAttribute("stats", stats);
        model.addAttribute("categorySvg", categorySvg);
        model.addAttribute("levelSvg", levelSvg);
        model.addAttribute("branchSvg", branchSvg);
        model.addAttribute("snapshots", snapshots);

        return "analytics/reports";
    }

    @PostMapping("/analytics/snapshot/create")
    public String createSnapshot(@RequestParam("reportType") String reportType,
                                 @RequestParam("title") String title,
                                 @RequestParam("academicYear") String academicYear,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        String actor = authentication != null ? authentication.getName() : "Institutional Admin";
        analyticsService.createReportSnapshot(reportType, title, academicYear, actor);
        redirectAttributes.addFlashAttribute("successMessage",
                "Accreditation report snapshot archived successfully with permanent timestamp.");
        return "redirect:/analytics";
    }
}
