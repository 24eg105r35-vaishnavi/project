package app.controller;

import app.model.Achievement;
import app.model.ProfileVisibility;
import app.model.Student;
import app.service.AchievementService;
import app.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PublicPortfolioController {

    private final StudentService studentService;
    private final AchievementService achievementService;

    @GetMapping("/portfolio/{identifier}")
    public String viewPublicPortfolio(@PathVariable("identifier") String identifier,
                                      Authentication authentication,
                                      Model model) {
        Optional<Student> studentOpt = studentService.findBySlugOrRoll(identifier);
        if (studentOpt.isEmpty()) {
            model.addAttribute("message", "Student profile not found for identifier: " + identifier);
            return "portfolio/not-found";
        }

        Student student = studentOpt.get();

        // Privacy Gate Check (Section 7.2 & 10)
        if (student.getProfileVisibility() == ProfileVisibility.PRIVATE) {
            model.addAttribute("student", student);
            model.addAttribute("isPrivate", true);
            return "portfolio/private-profile";
        }

        if (student.getProfileVisibility() == ProfileVisibility.COLLEGE_ONLY) {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login?redirect=/portfolio/" + identifier;
            }
        }

        // Only VERIFIED / APPROVED records appear publicly! (Section 7.2)
        List<Achievement> verifiedAchievements = achievementService.getVerifiedStudentAchievements(student);

        model.addAttribute("student", student);
        model.addAttribute("achievements", verifiedAchievements);
        model.addAttribute("totalPoints", student.getTotalPoints());
        model.addAttribute("verifiedCount", verifiedAchievements.size());

        // Prepare JSON-LD Structured Data
        String jsonLd = generateJsonLd(student, verifiedAchievements);
        model.addAttribute("jsonLd", jsonLd);

        return "portfolio/public-profile";
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<Student> publicStudents = studentService.getLeaderboard().stream()
                .filter(s -> s.getProfileVisibility() == ProfileVisibility.PUBLIC)
                .toList();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        xml.append("  <url><loc>https://portal.anurag.edu.in/</loc><changefreq>daily</changefreq><priority>1.0</priority></url>\n");
        xml.append("  <url><loc>https://portal.anurag.edu.in/explore</loc><changefreq>daily</changefreq><priority>0.8</priority></url>\n");

        for (Student s : publicStudents) {
            xml.append("  <url>\n");
            xml.append("    <loc>https://portal.anurag.edu.in/portfolio/").append(s.getRollNumber()).append("</loc>\n");
            xml.append("    <changefreq>weekly</changefreq>\n");
            xml.append("    <priority>0.9</priority>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>");
        return xml.toString();
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robotsTxt() {
        return """
                User-agent: *
                Allow: /
                Allow: /portfolio/
                Allow: /explore
                Allow: /about
                Disallow: /student/
                Disallow: /faculty/
                Disallow: /admin/
                Disallow: /vault/
                Disallow: /h2-console/
                
                Sitemap: https://portal.anurag.edu.in/sitemap.xml
                """;
    }

    private String generateJsonLd(Student student, List<Achievement> achievements) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"@context\": \"https://schema.org\",\n");
        sb.append("  \"@type\": \"Person\",\n");
        sb.append("  \"name\": \"").append(escapeJson(student.getUser().getFullName())).append("\",\n");
        sb.append("  \"identifier\": \"").append(escapeJson(student.getRollNumber())).append("\",\n");
        sb.append("  \"affiliation\": {\n");
        sb.append("    \"@type\": \"CollegeOrUniversity\",\n");
        sb.append("    \"name\": \"Anurag University, Hyderabad\"\n");
        sb.append("  },\n");
        sb.append("  \"hasCredential\": [\n");

        for (int i = 0; i < achievements.size(); i++) {
            Achievement a = achievements.get(i);
            sb.append("    {\n");
            sb.append("      \"@type\": \"EducationalOccupationalCredential\",\n");
            sb.append("      \"name\": \"").append(escapeJson(a.getTitle())).append("\",\n");
            sb.append("      \"credentialCategory\": \"").append(escapeJson(a.getCategory().getName())).append("\",\n");
            sb.append("      \"recognizedBy\": {\n");
            sb.append("        \"@type\": \"Organization\",\n");
            sb.append("        \"name\": \"").append(escapeJson(a.getOrganizer())).append("\"\n");
            sb.append("      }\n");
            sb.append("    }").append(i < achievements.size() - 1 ? "," : "").append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
