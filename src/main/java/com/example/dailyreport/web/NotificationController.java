package com.example.dailyreport.web;

import com.example.dailyreport.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String list(@RequestParam String userEmail, Model model) {
        model.addAttribute("notifications", notificationService.findByUserEmail(userEmail));
        model.addAttribute("userEmail", userEmail);
        return "notifications";
    }
}
