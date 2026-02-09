package com.example.dailyreport.web;

import com.example.dailyreport.service.NotificationService;
import com.example.dailyreport.repository.UserAccountRepository;
import com.example.dailyreport.service.CurrentUserProvider;
import com.example.dailyreport.domain.UserAccount;
import com.example.dailyreport.domain.UserRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {
    private final NotificationService notificationService;
    private final UserAccountRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public NotificationController(NotificationService notificationService,
                                  UserAccountRepository userRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/notifications")
    public String list(@RequestParam(required = false) Long userId, Model model) {
        UserAccount currentUser = currentUserProvider.getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;
        Long effectiveUserId = isAdmin ? userId : (currentUser != null ? currentUser.getId() : null);
        if (effectiveUserId == null && currentUser != null) {
            effectiveUserId = currentUser.getId();
        }
        if (effectiveUserId == null) {
            model.addAttribute("notifications", java.util.List.of());
            model.addAttribute("userId", null);
            model.addAttribute("user", null);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("currentUser", currentUser);
            return "notifications";
        }
        model.addAttribute("notifications", notificationService.findByUserId(effectiveUserId));
        model.addAttribute("userId", effectiveUserId);
        model.addAttribute("user", userRepository.findById(effectiveUserId).orElse(null));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUser", currentUser);
        return "notifications";
    }
}
