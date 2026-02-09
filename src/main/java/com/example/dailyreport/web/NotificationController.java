package com.example.dailyreport.web;

import com.example.dailyreport.service.NotificationService;
import com.example.dailyreport.repository.UserAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {
    private final NotificationService notificationService;
    private final UserAccountRepository userRepository;

    public NotificationController(NotificationService notificationService, UserAccountRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/notifications")
    public String list(@RequestParam Long userId, Model model) {
        model.addAttribute("notifications", notificationService.findByUserId(userId));
        model.addAttribute("userId", userId);
        model.addAttribute("user", userRepository.findById(userId).orElse(null));
        return "notifications";
    }
}
