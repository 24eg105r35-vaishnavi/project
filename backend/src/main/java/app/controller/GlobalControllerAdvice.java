package app.controller;

import app.model.Student;
import app.model.User;
import app.service.NotificationService;
import app.service.StudentService;
import app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;
    private final StudentService studentService;
    private final NotificationService notificationService;

    @ModelAttribute("currentUser")
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userService.findByEmail(authentication.getName()).orElse(null);
    }

    @ModelAttribute("currentStudent")
    public Student getCurrentStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Optional<User> user = userService.findByEmail(authentication.getName());
        return user.flatMap(studentService::findByUser).orElse(null);
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        Optional<User> user = userService.findByEmail(authentication.getName());
        return user.map(notificationService::getUnreadCount).orElse(0L);
    }
}
