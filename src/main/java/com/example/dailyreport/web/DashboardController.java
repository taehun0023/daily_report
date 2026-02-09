package com.example.dailyreport.web;

import com.example.dailyreport.repository.DailyReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final DailyReportRepository reportRepository;

    public DashboardController(DailyReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("reports", reportRepository.findTop7ByOrderByReportDateDesc());
        return "dashboard";
    }
}
