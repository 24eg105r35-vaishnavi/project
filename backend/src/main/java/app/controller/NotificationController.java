package app.controller;

import app.model.Notification;
import app.model.User;
import app.service.NotificationService;
import app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/notifications")
    public String viewNotifications(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        List<Notification> list = notificationService.getUserNotifications(user);
        model.addAttribute("notifications", list);
        return "notifications/list";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable("id") Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        notificationService.markAsRead(id, user);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllRead(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }
}
