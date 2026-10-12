package app.controller;

import app.model.Achievement;
import app.model.Student;
import app.repository.AchievementRepository;
import app.service.AnalyticsService;
import app.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AnalyticsService analyticsService;
    private final StudentService studentService;
    private final AchievementRepository achievementRepository;

    @GetMapping("/")
    public String index(Model model) {
        Map<String, Object> stats = analyticsService.getOverallStats();
        List<Student> topStudents = studentService.getLeaderboard();
        List<Achievement> recentVerified = achievementRepository.findAllApproved();

        model.addAttribute("stats", stats);
        model.addAttribute("topStudents", topStudents.stream().limit(5).toList());
        model.addAttribute("recentVerified", recentVerified.stream().limit(6).toList());
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
}
