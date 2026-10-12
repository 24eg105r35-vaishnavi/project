package app.controller;

import app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid institutional credentials. Please verify your email and password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully signed out.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("fullName") String fullName,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 @RequestParam("rollNumber") String rollNumber,
                                 @RequestParam("branch") String branch,
                                 @RequestParam("admissionYear") Integer admissionYear,
                                 @RequestParam("currentSemester") Integer currentSemester,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.registerStudent(email, password, fullName, phone, rollNumber, branch, admissionYear, currentSemester);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account registered successfully! You can now log in with your institutional credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect(Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"))) {
            return "redirect:/faculty/dashboard";
        }
        return "redirect:/student/dashboard";
    }
}
